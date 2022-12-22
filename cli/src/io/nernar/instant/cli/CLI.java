package io.nernar.instant.cli;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.List;

public class CLI {
    public static final int INVALID_KEY = 65534;
    public static final String INVALID_KEY_STROKE = String.valueOf((char) INVALID_KEY);
    private static boolean stdinIsConsole;
    private static Shell alteredShell;

    private interface Kernel32 extends Library {
        int GetConsoleMode(Pointer pointer, IntByReference intByReference);

        int SetConsoleMode(Pointer pointer, int i);

        Pointer GetStdHandle(int i);
    }

    private interface Libc extends Library {
        int tcgetattr(int i, Termios termios) throws LastErrorException;

        int tcsetattr(int i, int i2, Termios termios) throws LastErrorException;

        int isatty(int i);
    }

    private interface Msvcrt extends Library {
        int getwchar();

        int _getwch();

        int _kbhit();
    }

    public interface Shell {
        boolean capture() throws IOException;

        int read(boolean capture) throws IOException;

        void reset() throws IOException;
    }

    @SuppressWarnings("unused")
    private static final class Kernel32Defs {
        static final long INVALID_HANDLE_VALUE;
        static final int STD_INPUT_HANDLE = -10;
        static final int ENABLE_PROCESSED_INPUT = 1;
        static final int ENABLE_LINE_INPUT = 2;
        static final int ENABLE_ECHO_INPUT = 4;
        static final int ENABLE_WINDOW_INPUT = 8;

        private Kernel32Defs() {
        }

        static {
            INVALID_HANDLE_VALUE = Native.POINTER_SIZE == 8 ? -1L : 4294967295L;
        }
    }

    protected static final class Termios extends Structure {
        public int c_iflag;
        public int c_oflag;
        public int c_cflag;
        public int c_lflag;
        public byte c_line;
        public byte[] filler;

        Termios() {
            this.filler = new byte[64];
        }

        Termios(Termios t) {
            this.filler = new byte[64];
            this.c_iflag = t.c_iflag;
            this.c_oflag = t.c_oflag;
            this.c_cflag = t.c_cflag;
            this.c_lflag = t.c_lflag;
            this.c_line = t.c_line;
            this.filler = (byte[]) t.filler.clone();
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_line", "filler");
        }
    }

    @SuppressWarnings("unused")
    private static final class LibcDefs {
        static final int ISIG = 1;
        static final int ICANON = 2;
        static final int ECHO = 8;
        static final int ECHONL = 64;
        static final int TCSANOW = 0;

        private LibcDefs() {
        }
    }

    public static class WindowsShell implements Shell {
        private static Msvcrt msvcrt;
        private static Kernel32 kernel32;
        private static Pointer consoleHandle;
        private static int originalConsoleMode;

        public WindowsShell() {
            if (msvcrt == null || kernel32 == null) {
                msvcrt = (Msvcrt) Native.load("msvcrt", Msvcrt.class);
                kernel32 = (Kernel32) Native.load("kernel32", Kernel32.class);
            }
        }

        private static void prepareMaybe() {
            if (consoleHandle == null) {
                try {
                    consoleHandle = getStdInputHandle();
                    originalConsoleMode = getConsoleMode(consoleHandle);
                    CLI.stdinIsConsole = true;
                } catch (IOException e) {
                    CLI.stdinIsConsole = false;
                }
            }
        }

        public static Pointer getStdInputHandle() throws IOException {
            Pointer handle = kernel32.GetStdHandle(-10);
            if (Pointer.nativeValue(handle) == 0 || Pointer.nativeValue(handle) == Kernel32Defs.INVALID_HANDLE_VALUE) {
                throw new IOException("GetStdHandle(STD_INPUT_HANDLE)");
            }
            return handle;
        }

        public static int getConsoleMode(Pointer handle) throws IOException {
            IntByReference mode = new IntByReference();
            int rc = kernel32.GetConsoleMode(handle, mode);
            if (rc == 0) {
                throw new IOException("GetConsoleMode");
            }
            return mode.getValue();
        }

        public static void setConsoleMode(Pointer handle, int mode) throws IOException {
            int rc = kernel32.SetConsoleMode(handle, mode);
            if (rc == 0) {
                throw new IOException("SetConsoleMode");
            }
        }

        public static int getwch() {
            prepareMaybe();
            int c = msvcrt._getwch();
            if (c == 0 || c == 224) {
                int c2 = msvcrt._getwch();
                if (c2 >= 0 && c2 <= 6399) {
                    return 57344 + c2;
                }
                return 65534;
            } else if (c < 0 || c > 65535) {
                return 65534;
            } else {
                return c;
            }
        }

        @Override
        public boolean capture() throws IOException {
            prepareMaybe();
            return CLI.stdinIsConsole;
        }

        @Override
        public int read(boolean capture) throws IOException {
            prepareMaybe();
            if (!CLI.stdinIsConsole) {
                int c = msvcrt.getwchar();
                if (c == 65535) {
                    c = -1;
                }
                return c;
            }
            setConsoleMode(consoleHandle, originalConsoleMode & (-2));
            if (!capture && msvcrt._kbhit() == 0) {
                return -2;
            }
            return getwch();
        }

        @Override
        public void reset() throws IOException {
            if (consoleHandle != null) {
                setConsoleMode(consoleHandle, originalConsoleMode);
                consoleHandle = null;
            }
            CLI.stdinIsConsole = false;
        }
    }

    public static class UnixShell implements Shell {
        private static Libc libc;
        private static CharsetDecoder charsetDecoder;
        private static Termios originalTermios;
        private static Termios rawTermios;
        private static Termios intermediateTermios;

        public UnixShell() {
            if (libc == null || charsetDecoder == null) {
                libc = (Libc) Native.load("c", Libc.class);
                charsetDecoder = Charset.defaultCharset().newDecoder();
            }
        }

        private static void prepareMaybe() {
            if (CLI.stdinIsConsole) {
                return;
            }
            try {
                CLI.stdinIsConsole = libc.isatty(0) == 1;
                if (CLI.stdinIsConsole) {
                    originalTermios = getTerminalAttrs(0);
                    rawTermios = new Termios(originalTermios);
                    rawTermios.c_lflag &= -76;
                    intermediateTermios = new Termios(rawTermios);
                    intermediateTermios.c_lflag |= 2;
                }
            } catch (IOException e) {
                CLI.stdinIsConsole = false;
            }
        }

        public static Termios getTerminalAttrs(int fd) throws IOException {
            Termios termios = new Termios();
            try {
                int rc = libc.tcgetattr(fd, termios);
                if (rc != 0) {
                    throw new RuntimeException("tcgetattr");
                }
                return termios;
            } catch (LastErrorException e) {
                throw new IOException("tcgetattr", e);
            }
        }

        public static void setTerminalAttrs(int fd, Termios termios) throws IOException {
            prepareMaybe();
            try {
                int rc = libc.tcsetattr(fd, 0, termios);
                if (rc != 0) {
                    throw new RuntimeException("tcsetattr");
                }
            } catch (LastErrorException e) {
                throw new IOException("tcsetattr", e);
            }
        }

        private static int readSingleCharFromByteStream(InputStream inputStream) throws IOException {
            byte[] inBuf = new byte[4];
            int inLen = 0;
            while (inLen < inBuf.length) {
                int b = inputStream.read();
                if (b == -1) {
                    return -1;
                }
                int i = inLen;
                inLen++;
                inBuf[i] = (byte) b;
                int c = decodeCharFromBytes(inBuf, inLen);
                if (c != -1) {
                    return c;
                }
            }
            return 65534;
        }

        private static synchronized int decodeCharFromBytes(byte[] inBytes, int inLen) {
            charsetDecoder.reset();
            charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
            charsetDecoder.replaceWith(CLI.INVALID_KEY_STROKE);
            ByteBuffer in = ByteBuffer.wrap(inBytes, 0, inLen);
            CharBuffer out = CharBuffer.allocate(1);
            charsetDecoder.decode(in, out, false);
            if (out.position() == 0) {
                return -1;
            }
            return out.get(0);
        }

        @Override
        public boolean capture() throws IOException {
            prepareMaybe();
            return CLI.stdinIsConsole;
        }

        @Override
        public int read(boolean capture) throws IOException {
            prepareMaybe();
            if (!CLI.stdinIsConsole) {
                return readSingleCharFromByteStream(System.in);
            }
            setTerminalAttrs(0, rawTermios);
            if (!capture) {
                try {
                    if (System.in.available() == 0) {
                        setTerminalAttrs(0, intermediateTermios);
                        return -2;
                    }
                } catch (Throwable th) {
                    setTerminalAttrs(0, intermediateTermios);
                    throw th;
                }
            }
            int readSingleCharFromByteStream = readSingleCharFromByteStream(System.in);
            setTerminalAttrs(0, intermediateTermios);
            return readSingleCharFromByteStream;
        }

        @Override
        public void reset() throws IOException {
            setTerminalAttrs(0, originalTermios);
            CLI.stdinIsConsole = false;
        }
    }

    public static synchronized Shell byPlatform() {
        if (alteredShell != null) {
            return alteredShell;
        }
        if (System.getProperty("os.name").startsWith("Windows")) {
            alteredShell = new WindowsShell();
        } else {
            alteredShell = new UnixShell();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (CLI.stdinIsConsole) {
                    try {
                        CLI.alteredShell.reset();
                    } catch (IOException e) {
                    }
                }
            }
        }));
        return alteredShell;
    }

    public static synchronized boolean interactive() {
        if (stdinIsConsole) {
            return true;
        }
        Shell shell = byPlatform();
        try {
            if (shell.capture()) {
                shell.reset();
                return true;
            }
        } catch (IOException e) {
        }
        return stdinIsConsole;
    }
}
