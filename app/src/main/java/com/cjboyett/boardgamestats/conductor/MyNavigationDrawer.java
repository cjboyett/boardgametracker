package com.cjboyett.boardgamestats.conductor;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.SettingsActivity;
import com.cjboyett.boardgamestats.activity.addgameplay.AddGamePlayTabbedActivity;
import com.cjboyett.boardgamestats.activity.statsoverview.StatsTabbedActivity;
import com.cjboyett.boardgamestats.conductor.addgame.AddGameController;
import com.cjboyett.boardgamestats.conductor.collection.GameListController;
import com.cjboyett.boardgamestats.conductor.extras.ExtrasController;
import com.cjboyett.boardgamestats.conductor.main.MainController;

public class MyNavigationDrawer {
	private final Router router;
	private final DrawerLayout drawerLayout;
	private final NavigationView navigationView;

	public MyNavigationDrawer(Router router, DrawerLayout drawerLayout, NavigationView navigationView) {
		this.router = router;
		this.drawerLayout = drawerLayout;
		this.navigationView = navigationView;

		initializeNavigation();
	}

	private void initializeNavigation() {
		navigationView.setNavigationItemSelectedListener((item) -> {
			if (!item.isChecked()) {
				switch (item.getItemId()) {
					case R.id.menu_home:
						navigateToHome();
						break;
					case R.id.menu_collections:
						navigateToRootLevelController(new GameListController());
						break;
					case R.id.menu_add_game:
						navigateToRootLevelController(new AddGameController());
						break;
					case R.id.menu_stats:
						router.getActivity().startActivity(new Intent(router.getActivity(), StatsTabbedActivity.class));
						break;
					case R.id.menu_add_game_play:
						router.getActivity()
							  .startActivity(new Intent(router.getActivity(), AddGamePlayTabbedActivity.class));
						//						navigateToRootLevelController(new AddGamePlayTabbedController());
						break;
					case R.id.menu_extras:
						navigateToRootLevelController(new ExtrasController());
						break;
					case R.id.menu_settings:
						router.getActivity().startActivity(new Intent(router.getActivity(), SettingsActivity.class));
						break;
				}
			}
			drawerLayout.closeDrawers();
			return false;
		});
	}

	private void navigateToHome() {
		router.popToTag(MainController.TAG);
	}

	private void navigateToRootLevelController(Controller controller) {
		navigateToHome();
		router.pushController(RouterTransaction.with(controller));
	}
}
