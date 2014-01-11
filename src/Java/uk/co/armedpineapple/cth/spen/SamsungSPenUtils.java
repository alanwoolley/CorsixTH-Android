package uk.co.armedpineapple.cth.spen;

import uk.co.armedpineapple.cth.SDLActivity;

import com.samsung.spen.lib.input.SPenEventLibrary;

/**
 * Utility class for registering Samsung's SPen SDK listeners on the SDL
 * surface.
 * 
 * @author inagy
 */
public final class SamsungSPenUtils {
    private static final SPenEventLibrary sPenEventLibraryInstance = new SPenEventLibrary();

    private static final SamsungSPenHoverListener sPenHoverListenerInstance = new SamsungSPenHoverListener();
    private static final SamsungSPenTouchListener sPenTouchListenerInstance = new SamsungSPenTouchListener();

    /**
     * Utility class.
     */
    private SamsungSPenUtils() {
        // Private constructor
    }

    /**
     * Registers SPen listeners on the game area.
     */
    public static void registerListeners() {
        sPenEventLibraryInstance.setSPenTouchListener(SDLActivity.mSurface,
                sPenTouchListenerInstance);
		sPenEventLibraryInstance.setSPenHoverListener(SDLActivity.mSurface,
				sPenHoverListenerInstance);
	}
}