package uk.co.armedpineapple.cth;

import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.dialogs.MenuAdapter;
import uk.co.armedpineapple.cth.dialogs.MenuItems;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class SideMenu extends RelativeLayout {

	Context			ctx;
	Animation		outanim;
	Animation		inanim;
	boolean			showing	= false;
	ListView		list;
	MenuAdapter	adapter;

	// Constructors

	public SideMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SideMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SideMenu(Context context) {
		super(context);
		init(context);
	}

	public void init(Context context) {
		this.ctx = context;
		outanim = AnimationUtils.loadAnimation(ctx, R.animator.menu_slideout);
		inanim = AnimationUtils.loadAnimation(ctx, R.animator.menu_slidein);

		list = new ListView(context);
		adapter = new MenuAdapter(context, MenuItems.getItems());
		list.setAdapter(adapter);
		list.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		addView(list);

	}

	// Visibility

	public void show() {
		Log.d(getClass().getSimpleName(), "Showing side menu");
		setVisibility(VISIBLE);
		startAnimation(inanim);
		showing = true;
	}

	public void hide() {
		Log.d(getClass().getSimpleName(), "Hiding side menu");
		startAnimation(outanim);
		showing = false;
	}

	public void toggle() {
		if (showing) {
			hide();
		} else {
			show();
		}

	}

	@Override
	protected void onAnimationEnd() {
		super.onAnimationEnd();
		setVisibility(GONE);
	}

	@Override
	protected void onAnimationStart() {
		super.onAnimationStart();
		setVisibility(VISIBLE);
	}
}
