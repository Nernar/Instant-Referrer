package com.nernar.instant.storage.external;

import com.nernar.instant.storage.AbstractResource;
import com.nernar.instant.storage.TranslationResource;

public class InstantConfigInformation extends AbstractResource {
	{
		put(new TranslationResource("description",
			"Miscellaneous Instant Referrer options and several internal visual interface patches.",
			"Основные настройки среды раннего запуска и некоторых частей встроенных патчей интерфейса."));
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
					"Среда"));
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
					"Информативный прогресс"));
				put("description", "Показывать больше информации в выполняемых задачах, в том числе их количество и затраченное время.");
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
					"Безграничный режим"));
				put("description", "Расширять содержимое окна, делая системные панели навигации прозрачными.");
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
					"Перезапись авто-запуска"));
				put("description", "Игнорировать входящий системный флаг авто-запуска, заменяя его встроенным флажком.");
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
					"Возможность отмены"));
				put("description", "Добавить возможность отменить запуск пака, не перезапуская приложение.");
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
					"Компоновка"));
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
					"Перемешать арты"));
				put("description", "Случайно изменять порядок фоновых артов после каждого запуска.");
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
					"Полноэкранный режим"));
				put("description", "Растянуть изображение компоновки на весь экран.");
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
					"Распространение"));
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
					"Наличие Майнкрафта"));
				put("description", "Игнорировать наличие установленной игре на устройстве, обозначая прочую собственную покупку.");
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
					"Скрытие предупреждения"));
				put("description", "Скрывать предупреждение об отсуствии игры, следуя условиям лицензирования.");
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
					"Объявления"));
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
					"Поддержка модификаций"));
				put("description", "Поддерживать разработчиков модификаций и создателей Inner Core, добавляя рекламные карточки.");
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
					"Блокировка всего"));
				put("description", "Запретить добавление любых рекламных объявлений, отказываясь от поддержки создателей Horizon.");
				put("index", 13);
			}
			
			@Override
			public String getId() {
				return Properties.this.getId() + ":advertisement.block_everything";
			}
		}
	}
}
