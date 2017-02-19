package com.cjboyett.boardgamestats.scoop;

import com.cjboyett.boardgamestats.view.scoop.MainUiContainer;
import com.lyft.scoop.RouteChange;
import com.lyft.scoop.Router;

public class AppRouter extends Router {
	public static final String SERVICE_NAME = "app_router_service";
	private MainUiContainer uiContainer;

	public AppRouter() {
		super();
	}

	public AppRouter(MainUiContainer uiContainer) {
		this.uiContainer = uiContainer;
	}

	@Override
	protected void onRouteChanged(RouteChange routeChange) {
		uiContainer.goTo(routeChange);
	}

	public void setUiContainer(MainUiContainer uiContainer) {
		this.uiContainer = uiContainer;
	}
}
