package io.nernar.instant.storage.external;

import io.nernar.instant.prebuilt.InstantConfigSource;
import io.nernar.instant.storage.AbstractResource;
import com.zhekasmirnov.innercore.utils.FileTools;
import java.io.File;

public class InstantConfig extends AbstractResource {
	{
		put(new Environment());
		put(new Background());
		put(new Recycler());
		put(new Distribution());
		put(new Advertisement());
	}
	
	@Override
	public String getId() {
		return "instant.json";
	}
	
	@Override
	public File getOutput() {
		return new File(FileTools.DIR_WORK, getId());
	}
	
	protected class Environment extends AbstractResource {
		{
			put("informative_progress", InstantConfigSource.Environment.INFORMATIVE_PROGRESS);
			put("immersive_mode", InstantConfigSource.Environment.IMMERSIVE_MODE);
			put("auto_launch", InstantConfigSource.Environment.AUTO_LAUNCH);
			put("auto_launch_override", InstantConfigSource.Environment.AUTO_LAUNCH_OVERRIDE);
			put("abort_ability", InstantConfigSource.Environment.ABORT_ABILITY);
		}
		
		@Override
		public String getId() {
			return InstantConfig.this.getId() + ":environment";
		}
	}
	
	protected class Background extends AbstractResource {
		{
			put("shuffle_art", InstantConfigSource.Background.SHUFFLE_ART);
			put("frame_duration", InstantConfigSource.Background.FRAME_DURATION);
			put("smooth_movement", InstantConfigSource.Background.SMOOTH_MOVEMENT);
			put("force_fullscreen", InstantConfigSource.Background.FORCE_FULLSCREEN);
			put("brightness", InstantConfigSource.Background.BRIGHTNESS);
		}
		
		@Override
		public String getId() {
			return InstantConfig.this.getId() + ":background";
		}
	}
	
	protected class Recycler extends AbstractResource {
		{
			put("measure_to_bottom", InstantConfigSource.Recycler.MEASURE_TO_BOTTOM);
			put("width_modifier", InstantConfigSource.Recycler.WIDTH_MODIFIER);
			put("card_padding", InstantConfigSource.Recycler.CARD_PADDING);
			put("card_radius_modifier", InstantConfigSource.Recycler.CARD_RADIUS_MODIFIER);
		}
		
		@Override
		public String getId() {
			return InstantConfig.this.getId() + ":recycler";
		}
	}
	
	protected class Distribution extends AbstractResource {
		{
			put("had_minecraft", InstantConfigSource.Distribution.HAD_MINECRAFT);
			put("dismiss_warning", InstantConfigSource.Distribution.DISMISS_WARNING);
		}
		
		@Override
		public String getId() {
			return InstantConfig.this.getId() + ":distribution";
		}
	}
	
	protected class Advertisement extends AbstractResource {
		{
			put("support_modification", InstantConfigSource.Advertisement.SUPPORT_MODIFICATION);
			put("block_everything", InstantConfigSource.Advertisement.BLOCK_EVERYTHING);
		}
		
		@Override
		public String getId() {
			return InstantConfig.this.getId() + ":advertisement";
		}
	}
}
