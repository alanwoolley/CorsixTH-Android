package uk.co.armedpineapple.corsixth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

public class SideMenu extends RelativeLayout {

	Context ctx;
	Animation outanim;
	Animation inanim;
	boolean showing = false;

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
	}

	public void show() {
		setVisibility(VISIBLE);
		startAnimation(inanim);
		showing = true;
	}

	public void hide() {
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
