package com.cjboyett.boardgamestats.scoop;

import com.cjboyett.boardgamestats.view.scoop.DialogUiContainer;
import com.lyft.scoop.RouteChange;
import com.lyft.scoop.Router;
import com.lyft.scoop.Screen;

public class DialogRouter extends Router {
	public static final String SERVICE_NAME = "dialog_router_service";
	private DialogUiContainer uiContainer;
	private boolean blocking;
	private boolean dialogShowing;

	public DialogRouter() {
		super();
	}

	public DialogRouter(DialogUiContainer uiContainer) {
		this.uiContainer = uiContainer;
	}

	@Override
	protected void onRouteChanged(RouteChange routeChange) {
		uiContainer.goTo(routeChange);
	}

	public void setUiContainer(DialogUiContainer uiContainer) {
		this.uiContainer = uiContainer;
	}

	public boolean dismiss() {
		dialogShowing = false;
		return blocking || goBack();
	}

	public void show(Screen screen) {
		dialogShowing = true;
		replaceAllWith(screen);
	}

	public boolean isDialogShowing() {
		return dialogShowing;
	}

	public void blockGoBack() {
		blocking = true;
	}

	public void unblockGoBack() {
		blocking = false;
	}
}
