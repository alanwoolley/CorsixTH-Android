package uk.co.armedpineapple.corsixth.wizard;

import uk.co.armedpineapple.corsixth.Configuration;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public abstract class WizardView extends RelativeLayout {

	public WizardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public WizardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	public WizardView(Context context) {
		super(context);
		
	}

	abstract void saveConfiguration(Configuration config);

	abstract void loadConfiguration(Configuration config);

}
