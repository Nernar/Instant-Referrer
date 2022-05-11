package io.nernar.instant.storage.external;

import io.nernar.instant.storage.AbstractResource;
import io.nernar.instant.storage.TranslationResource;

public class InstantConfigInformation extends AbstractResource {
	{
		put(new TranslationResource("description",
			"Miscellaneous Instant Referrer options and several internal visual interface patches.",
			"Основные настройки среды раннего запуска и некоторых частей встроенных патчей интерфейса.",
			"Основні налаштування середовища раннього запуску і деяких частин вбудованих патчів інтерфейсу."));
		put(new Properties());
	}
	
	@Override
	public String getId() {
		return "instant.info.json";
	}
	
	protected class Properties extends AbstractResource {
		{
			put(new Environment());
			put(new EnvironmentInformativeProgress());
			put(new EnvironmentImmersiveMode());
			put(new EnvironmentAutoLaunch());
			put(new EnvironmentAutoLaunchOverride());
			put(new EnvironmentAbortAbility());
			put(new Background());
			put(new BackgroundShuffleArt());
			put(new BackgroundFrameDuration());
			put(new BackgroundSmoothMovement());
			put(new BackgroundForceFullscreen());
			put(new BackgroundBrightness());
			put(new Recycler());
			put(new Distribution());
			put(new DistributionHadMinecraft());
			put(new DistributionDismissWarning());
			put(new Advertisement());
			put(new AdvertisementSupportModification());
			put(new AdvertisementBlockEverything());
		}
		
		@Override
		public String getId() {
			return InstantConfigInformation.this.getId() + ":properties";
		}
		
		protected class Environment extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Environment",
					"Среда",
					"Середовище"));
				put("collapsible", false);
				put("index", 0);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":environment";
			}
		}
		
		protected class EnvironmentInformativeProgress extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Informative progress",
					"Информативный прогресс",
					"Інформативний прогрес"));
				put(new TranslationResource("description",
					"Show more information in performed tasks, including their count and spent time.",
					"Показывать больше информации в выполняемых задачах, в том числе их количество и затраченное время.",
					"Показувати більше інформації у виконуваних задачах, в тому числі їх кількість і витрачений час."));
				put("index", 1);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":environment.informative_progress";
			}
		}
		
		protected class EnvironmentImmersiveMode extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Immersive mode",
					"Безграничный режим",
					"Безрамковий режим"));
				put(new TranslationResource("description",
					"Expand window contents, making system navigation panels transparent.",
					"Расширять содержимое окна, делая системные панели навигации прозрачными.",
					"Розширювати вміст вікна, роблячи системні панелі навігації прозорими."));
				put("index", 2);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":environment.immersive_mode";
			}
		}
		
		protected class EnvironmentAutoLaunch extends AbstractResource {
			{
				put("display", false);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":environment.auto_launch";
			}
		}
		
		protected class EnvironmentAutoLaunchOverride extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Auto-launch Override",
					"Перезапись авто-запуска",
					"Перезапис авто-запуску"));
				put(new TranslationResource("description",
					"Ignore incoming auto-start system flag, replacing it with a built-in flag.",
					"Игнорировать входящий системный флаг авто-запуска, заменяя его встроенным флажком.",
					"Ігнорувати вхідний системний прапорець авто-запуску, замінюючи його вбудованим прапорцем."));
				put("index", 3);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":environment.auto_launch_override";
			}
		}
		
		protected class EnvironmentAbortAbility extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Abort Ability",
					"Возможность отмены",
					"Можливість відмінии"));
				put(new TranslationResource("description",
					"Add ability to cancel pack launch without restarting application.",
					"Добавить возможность отменить запуск пака, не перезапуская приложение.",
					"Додати можливість відмінювати запуск паку не перезавантажуючи програму."));
				put("index", 4);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":environment.abort_ability";
			}
		}
		
		protected class Background extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Background",
					"Компоновка",
					"Фон"));
				put("collapsible", false);
				put("index", 5);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":background";
			}
		}
		
		protected class BackgroundShuffleArt extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Shuffle Art",
					"Перемешать арты",
					"Змішати арти"));
				put(new TranslationResource("description",
					"Randomly change order of background art after each launch.",
					"Случайно изменять порядок фоновых артов после каждого запуска.",
					"Випадково змінювати порядок фонових артів після кожного запуску."));
				put("index", 6);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":background.shuffle_art";
			}
		}
		
		protected class BackgroundFrameDuration extends AbstractResource {
			{
				put("display", false);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":background.frame_duration";
			}
		}
		
		protected class BackgroundSmoothMovement extends AbstractResource {
			{
				put("display", false);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":background.smooth_movement";
			}
		}
		
		protected class BackgroundForceFullscreen extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Force Fullscreen",
					"Полноэкранный режим",
					"Повноекранний режим"));
				put(new TranslationResource("description",
					"Stretch content layout image to full screen.",
					"Растянуть изображение компоновки на весь экран.",
					"Растянуть изображение компоновки на весь экран."));
				put("index", 7);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":background.force_fullscreen";
			}
		}
		
		protected class BackgroundBrightness extends AbstractResource {
			{
				put("display", false);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":background.brightness";
			}
		}
		
		protected class Recycler extends AbstractResource {
			{
				put("display", false);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":recycler";
			}
		}
		
		protected class Distribution extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Distribution",
					"Распространение",
					"Розповсюдження"));
				put("collapsible", false);
				put("index", 8);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":distribution";
			}
		}
		
		protected class DistributionHadMinecraft extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Had Minecraft",
					"Наличие Майнкрафта",
					"Наявність Майнкрафту"));
				put(new TranslationResource("description",
					"Ignore presence of device installed game, indicating other own purchase.",
					"Игнорировать наличие установленной игре на устройстве, обозначая прочую собственную покупку.",
					"Ігнорувати наявність уставновленої гри на пристрої, позначаючи іншу особисту покупку."));
				put("index", 9);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":distribution.had_minecraft";
			}
		}
		
		protected class DistributionDismissWarning extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Dismiss Warning",
					"Скрытие предупреждения",
					"Приховані попередження"));
				put(new TranslationResource("description",
					"Hide warning about game absense, following licensing conditions.",
					"Скрывать предупреждение об отсуствии игры, следуя условиям лицензирования.",
					"Приховувати попередження про відсутність гри, слідуючи умовам ліцензування."));
				put("index", 10);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":distribution.dismiss_warning";
			}
		}
		
		protected class Advertisement extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Advertisement",
					"Объявления",
					"Оголошення"));
				put("collapsible", false);
				put("index", 11);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":advertisement";
			}
		}
		
		protected class AdvertisementSupportModification extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Support Modification",
					"Поддержка модификаций",
					"Підтримка модифікацій"));
				put(new TranslationResource("description",
					"Support modification developers and Inner Core creators by adding promotional cards.",
					"Поддерживать разработчиков модификаций и создателей Inner Core, добавляя рекламные карточки.",
					"Підтримувати розробників модифікацій і творців Inner Core, додаючи рекламні картки."));
				put("index", 12);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":advertisement.support_modification";
			}
		}
		
		protected class AdvertisementBlockEverything extends AbstractResource {
			{
				put(new TranslationResource("name",
					"Block Everything",
					"Блокировка всего",
					"Блокування всього"));
				put(new TranslationResource("description",
					"Prohibit addition of any advertisements, refusing to support creators of Horizon.",
					"Запретить добавление любых рекламных объявлений, отказываясь от поддержки создателей Horizon.",
					"Заборонити додавання будь-яких рекламних оголошень, відмовляючись від підтримки творців Horizon."));
				put("index", 13);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":advertisement.block_everything";
			}
		}
	}
}
