package net.anfoya.movie.search.javafx.entrypoint;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import net.anfoya.java.net.filtered.easylist.EasyListRuleSet;
import net.anfoya.java.net.filtered.easylist.proxy.Proxy;
import net.anfoya.java.net.url.filter.RuleSet;
import net.anfoya.movie.search.javafx.ComponentBuilder;
import net.anfoya.movie.search.javafx.SearchPane;
import net.anfoya.movie.search.javafx.SearchTabs;

public class MovieSearchApp extends Application {

	private static final int PORT = 666;

	public static void main(final String[] args) {
		new Thread(() -> {
			try {
				final RuleSet ruleSet = new EasyListRuleSet(false);
				ruleSet.load();
				new Proxy(PORT, ruleSet);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		});

		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "" + PORT);
		//		System.setProperty("https.proxyHost", "127.0.0.1");
		//		System.setProperty("https.proxyPort", "666");

		launch(args);
	}

	private final SearchTabs searchTabs;
	private final SearchPane searchPane;

	public MovieSearchApp() {
		final ComponentBuilder compBuilder = new ComponentBuilder();
		searchTabs = compBuilder.buildSearchTabs();
		searchPane = compBuilder.buildSearchPane();
	}

	@Override
	public void init() throws Exception {
		super.init();
	}

	@Override
	public void start(final Stage mainStage) {
		initGui(mainStage);
		initData();
	}

	private void initGui(final Stage mainStage) {
		final BorderPane mainPane = new BorderPane();
		mainPane.setTop(searchPane);
		mainPane.setCenter(searchTabs);

		searchTabs.setOnSearched(search -> {
			searchPane.setSearched(search);
			return null;
		});
		searchPane.setOnSearchAction(resultVo -> {
			searchTabs.search(resultVo);
			return null;
		});

		final Scene scene = new Scene(mainPane, 1150, 800);
		scene.getStylesheets().add(getClass().getResource("/net/anfoya/javafx/scene/control/button_flat.css").toExternalForm());
		scene.getStylesheets().add(getClass().getResource("/net/anfoya/javafx/scene/control/combo_noarrow.css").toExternalForm());

		mainStage.setTitle("Movie Search");
		mainStage.getIcons().add(new Image(getClass().getResourceAsStream("MovieSearch.png")));
		mainStage.setScene(scene);
		mainStage.show();
	}

	private void initData() {
		searchTabs.init();
	}
}

