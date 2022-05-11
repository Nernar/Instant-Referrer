package io.nernar.instant.prebuilt;

import io.nernar.instant.storage.AbstractResource;
import io.nernar.instant.storage.TranslationResource;
import java.util.Arrays;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

public class InstantTranslation extends TranslationResource {
	private static final InstantTranslation SINGLETON = new InstantTranslation();
	public static final String DEFAULT_LOCALE = Locale.ENGLISH.getLanguage();
	
	private InstantTranslation() {
		super("translation.json");
	}
	
	public static String getLanguage() {
		return Locale.getDefault().getLanguage();
	}
	
	public static JSONArray getAvailabledLanguages() {
		return SINGLETON.names();
	}
	
	public static boolean hasLanguage(String language) {
		return SINGLETON.has(language);
	}
	
	public static String translate(String language, String key, String fallback) {
		if (!hasLanguage(language)) {
			return fallback;
		}
		JSONObject obj = SINGLETON.optJSONObject(language);
		if (obj != null) {
			return obj.optString(key, fallback);
		}
		return fallback;
	}
	
	public static String translate(String language, String key) {
		return translate(language, key, key);
	}
	
	public static String findTranslation(String key, String... priority) {
		for (int i = 0; i < priority.length; i++) {
			String language = priority[i];
			String value = translate(language, key, null);
			if (value != null) return value;
		}
		JSONArray availabled = getAvailabledLanguages();
		for (int i = 0; i < availabled.length(); i++) {
			String language = availabled.optString(i);
			if (Arrays.binarySearch(priority, language) >= 0) continue;
			String value = translate(language, key, null);
			if (value != null) return value;
		}
		return key;
	}
	
	public static String translate(String key) {
		String language = getLanguage();
		if (DEFAULT_LOCALE != language) {
			return findTranslation(key, language, DEFAULT_LOCALE);
		}
		return findTranslation(key, DEFAULT_LOCALE);
	}
	
	public static InstantTranslation toSingleton() {
		return SINGLETON;
	}
	
	{
		put(new ENGLISH());
		put(new RUSSIAN());
		put(new UKRAINIAN());
	}
	
	public final class ENGLISH extends AbstractResource {
		{
			put("instant_referrer", "Instant Referrer");
			put("exit", "Exit");
			put("proceed", "Proceed");
			put("yes", "Yes");
			put("no", "No");
			put("abort", "Abort");
			put("restart", "Restart");
			put("hold_to_abort", "Hold button to abort launch");
			put("abort_not_supported", "Abort is not supported");
			put("instant_not_supported", "Instant Referrer is not supported yet");
			put("fail_launch", "Could not launch");
			put("another_instance_running", "Another instance of Inner Core previously loaded, so Instant Referrer cannot be resolved.");
			put("restart_to_launch", "Do you want to restart application for restore it?");
			put("instant_wait", "SYNC INSTANT");
			put("instant_build", "PRELOADING");
			put("instant_run", "BUILDING");
			put("fail_remove_launch_button", "Could not remove launch button");
			put("fail_setup_informative_progress", "Could not setup informative progress");
			put("fail_make_immersive", "Could not make immersive mode");
			put("fail_restyle_padding_recycler", "Could not restyle padding recycler");
			put("fail_change_recycler_gravity", "Could not change recycler gravity");
			put("fail_measure_recycler_layout", "Could not measure recycler layout");
			put("fail_add_auto_launch_button", "Could not add auto-launch button");
			put("fail_patch_abort_ability", "Could not patch abort ability");
			put("fail_remove_recycler_decoration", "Could not remove recycler decoration");
			put("fail_measure_and_clip_background", "Could not measure and clip background");
			put("fail_remove_distribution_nodes", "Could not remove distribution nodes");
			put("fail_close_advertisement_requests", "Could not close advertisement requests");
			put("fail_cleanup_exiting_containers", "Could not cleanup exiting containers");
			put("continue_decision", "Do you want to continue?");
			put("instant_startup_interrupted", "Instant Referrer startup process interrupted or something went wrong when modules loading.");
			put("resource_override_invocation", "Resource directories override failed");
			put("flipbook_descriptor_invocation", "Failed to initialize flipbook texture atlas descriptor");
			put("material_processor_invocation", "Failed to initialize material processor");
			put("content_manager_invocation", "Failed to initialize main content manager");
		}
		
		@Override
		public String getId() {
			return InstantTranslation.this.getId() + ":en";
		}
	}
	
	public final class RUSSIAN extends AbstractResource {
		{
			put("instant_referrer", "Среда раннего запуска");
			put("exit", "Выход");
			put("proceed", "Принять");
			put("yes", "Да");
			put("no", "Нет");
			put("abort", "Отмена");
			put("restart", "Рестарт");
			put("hold_to_abort", "Удерживайте кнопку для отмены запуска");
			put("abort_not_supported", "Отмена запуска не поддерживается");
			put("instant_not_supported", "Среда раннего запуска пока не поддерживается");
			put("fail_launch", "Не удалось произвести запуск");
			put("another_instance_running", "Другой пак Inner Core уже был загружен, так что среда раннего запуска не может быть использована.");
			put("restart_to_launch", "Вы желаете перезапустить приложение, чтобы восстановить ее?");
			put("instant_wait", "ИЩЕМ СРЕДУ");
			put("instant_build", "ПОДГОТОВКА");
			put("instant_run", "СБОРКА");
			put("fail_remove_launch_button", "Не удалось удалить кнопку запуска");
			put("fail_setup_informative_progress", "Не удалось установить информативный прогресс");
			put("fail_make_immersive", "Не удалось перейти в безграничный режим");
			put("fail_restyle_padding_recycler", "Не удалось добавить отступы в меню");
			put("fail_change_recycler_gravity", "Не удалось изменить гравитацию меню");
			put("fail_measure_recycler_layout", "Не удалось растянуть разметку меню");
			put("fail_add_auto_launch_button", "Не удалось добавить флажок авто-запуска");
			put("fail_patch_abort_ability", "Не удалось обозначить возможность отмены");
			put("fail_remove_recycler_decoration", "Не удалось удалить разделители меню");
			put("fail_measure_and_clip_background", "Не удалось растянуть и обрезать компоновку");
			put("fail_remove_distribution_nodes", "Не удалось удалить узлы распространения");
			put("fail_close_advertisement_requests", "Не удалось закрыть рекламные запросы");
			put("fail_cleanup_exiting_containers", "Не удалось очистить существующие контейнеры");
			put("continue_decision", "Вы все равно хотите продолжить?");
			put("instant_startup_interrupted", "Запуск среды раннего запуска был прерван или что-то произошло во время загрузки модулей.");
			put("resource_override_invocation", "Перезапись расположения ресурсов провалилась");
			put("flipbook_descriptor_invocation", "Создание описаний анимированных текстур провалилось");
			put("material_processor_invocation", "Создание обработчика материалов провалилось");
			put("content_manager_invocation", "Создание основного обработчика ресурсов провалилось");
		}
		
		@Override
		public String getId() {
			return InstantTranslation.this.getId() + ":ru";
		}
	}
	
	public final class UKRAINIAN extends AbstractResource {
		{
			put("instant_referrer", "Середовище раннього запуску");
			put("exit", "Вийти");
			put("proceed", "Продовжити");
			put("yes", "Так");
			put("no", "Ні");
			put("abort", "Відміна");
			put("restart", "Рестарт");
			put("hold_to_abort", "Утримуйте клавішу для відміни запуску");
			put("abort_not_supported", "Відміна запуску не підтримується");
			put("instant_not_supported", "Середовище раннього запуску поки що не підтримується");
			put("fail_launch", "Не вдалось провести запуск");
			put("another_instance_running", "Інший пак Inner Core вже був завантажений, тому середовище раннього запуску не може бути використано.");
			put("restart_to_launch", "Ви бажаєте перезавантажити програму, щоб відновити його?");
			put("instant_wait", "ШУКАЄМО СЕРЕДОВИЩЕ");
			put("instant_build", "ПІДГОТУВАННЯ");
			put("instant_run", "ЗБІРКА");
			put("fail_remove_launch_button", "Не вдалося видалити клавішу запуску");
			put("fail_setup_informative_progress", "Не вдалося встановити інформаційний прогрес");
			put("fail_make_immersive", "Не вдалося перейти в безрамковий режим");
			put("fail_restyle_padding_recycler", "Не вдалося додати відступи в меню");
			put("fail_change_recycler_gravity", "Не вдалося змінити гравітаційне притяжіння меню");
			put("fail_measure_recycler_layout", "Не вдалося розтягнути макет меню");
			put("fail_add_auto_launch_button", "Не вдалося додати прапорець автозапуску");
			put("fail_patch_abort_ability", "Не вдалося зазначити можливіть відміни");
			put("fail_remove_recycler_decoration", "Не вдалося видалити розмежування пунктів меню");
			put("fail_measure_and_clip_background", "Не вдалося розтягнути й обрізати фон");
			put("fail_remove_distribution_nodes", "Не вдалося видалити нотатки для розповсюдження");
			put("fail_close_advertisement_requests", "Не вдалося закрити рекламні запити");
			put("fail_cleanup_exiting_containers", "Не вдалося очистити існуючі контейнери");
			put("continue_decision", "Ви все одно бажаєте продовжити?");
			put("instant_startup_interrupted", "Старт середовища ранього запуску був перерваний або щось відбулось під час завантаження модулів.");
			put("resource_override_invocation", "Перезапис розміщення ресурсів зазнав краху");
			put("flipbook_descriptor_invocation", "Створення описів анімованих текстур зазнало краху");
			put("material_processor_invocation", "Створення обробника матеріалів зазнало краху");
			put("content_manager_invocation", "Створення основного обробника ресурсів зазнало краху");
		}
		
		@Override
		public String getId() {
			return InstantTranslation.this.getId() + ":uk";
		}
	}
}
