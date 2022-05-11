package io.nernar.instant.prebuilt;

public final class InstantConfigSource {
	
	public static final class Environment {
		public static final boolean INFORMATIVE_PROGRESS = true;
		public static final boolean IMMERSIVE_MODE = true;
		public static final boolean AUTO_LAUNCH = false;
		public static final boolean AUTO_LAUNCH_OVERRIDE = false;
		public static final boolean ABORT_ABILITY = true;
	}
	
	public static final class Background {
		public static final boolean SHUFFLE_ART = true;
		public static final int FRAME_DURATION = 90;
		public static final boolean SMOOTH_MOVEMENT = false;
		public static final boolean FORCE_FULLSCREEN = true;
		public static final double BRIGHTNESS = 0.4d;
	}
	
	public static final class Recycler {
		public static final boolean MEASURE_TO_BOTTOM = true;
		public static final double WIDTH_MODIFIER = 1.15d;
		public static final boolean CARD_PADDING = true;
		public static final int CARD_RADIUS_MODIFIER = 8;
	}
	
	public static final class Distribution {
		public static final boolean HAD_MINECRAFT = false;
		public static final boolean DISMISS_WARNING = false;
	}
	
	public static final class Advertisement {
		public static final boolean SUPPORT_MODIFICATION = true;
		public static final boolean BLOCK_EVERYTHING = false;
	}
}
