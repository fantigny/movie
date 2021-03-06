package net.anfoya.movie.browser.javafx.movie;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.anfoya.javafx.scene.control.Title;
import net.anfoya.javafx.scene.control.TitledProgressBar;
import net.anfoya.movie.browser.Config;
import net.anfoya.movie.browser.model.Movie;
import net.anfoya.movie.browser.model.Profile;
import net.anfoya.movie.browser.model.Tag;
import net.anfoya.movie.browser.model.Website;
import net.anfoya.movie.browser.service.MovieService;
import net.anfoya.movie.browser.service.MovieTagService;


public class MoviePane extends BorderPane {
	private static final String NO_MOVIE_TITLE = "Select a movie";
	private static final String MULTIPLE_MOVIE_TITLE = "Multiple movies";

	private Set<Movie> movies;

	private final VBox movieBox;

	private final Title title;
	private final TabPane webPanes;
	private final MovieToolboxPane toolboxPane;
	private final MovieTagsPane tagsPane;

	public MoviePane(final MovieService movieService, final MovieTagService tagService, final Profile profile) {
		this.movies = new LinkedHashSet<Movie>();

		title = new Title(NO_MOVIE_TITLE);
		setTop(title);

		// web pages
		webPanes = new TabPane();
		webPanes.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		webPanes.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
		setCenter(webPanes);

		// tags
		tagsPane = new MovieTagsPane(tagService);
		tagsPane.setExpanded(false);

		// tool box
		toolboxPane = new MovieToolboxPane(movieService, tagService, webPanes);
		toolboxPane.advancedProperty().bind(tagsPane.expandedProperty());

		// box is displayed when at least a movie is selected
		if (profile != Profile.RESTRICTED) {
			movieBox = new VBox(toolboxPane, tagsPane);
		} else {
			movieBox = new VBox(toolboxPane);
		}

		setMargin(title, new Insets(8, 5, 3, 10));
		setMargin(webPanes, new Insets(0, 5, 5, 5));
		setMargin(movieBox, new Insets(0, 5, 5, 5));

		// initialize web tabs
		for(final Website website: new Config().getWebsites()) {
			final MovieWebPane webPanel = new MovieWebPane(website);
			webPanel.setOnSavePage(toolboxPane.getOnSavePage());

			final TitledProgressBar progressTitle = new TitledProgressBar(website.getName());
			progressTitle.setPrefWidth(120);
			progressTitle.progressProperty().bind(webPanel.progressProperty());
			progressTitle.stateProperty().bind(webPanel.stateProperty());

			final Tab tab = new Tab();
			tab.setGraphic(progressTitle);
			tab.setContent(webPanel);
			tab.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(final ObservableValue<? extends Boolean> ov, final Boolean oldVal, final Boolean newVal) {
					if (newVal) {
						webPanel.load(movies, tab.isSelected());
					}
				}
			});

			webPanes.getTabs().add(tab);
		}
	}

	public void load(final Set<Movie> movies) {
		this.movies = movies;

		loadWebPane();
		updateTitle();

		toolboxPane.refresh(movies);

		tagsPane.refresh(movies);

		setDisplayMovieBox(!movies.isEmpty());
	}

	public void refreshTags() {
		tagsPane.refresh();
	}

	public void setOnAddTag(final EventHandler<ActionEvent> handler) {
		tagsPane.setOnAddTag(handler);
	}

	public void setOnDelTag(final EventHandler<ActionEvent> handler) {
		tagsPane.setOnDelTag(handler);
	}

	public void setOnCreateTag(final EventHandler<ActionEvent> handler) {
		toolboxPane.setOnCreateTag(handler);
	}

	public void setOnUpdateMovie(final EventHandler<ActionEvent> handler) {
		toolboxPane.setOnUpdateMovie(handler);
	}

	protected TabPane getWebPanes() {
		return webPanes;
	}

	protected MovieToolboxPane getToolboxPane() {
		return toolboxPane;
	}

	protected MovieTagsPane getTagsPane() {
		return tagsPane;
	}

	private void loadWebPane() {
		webPanes.getTabs().forEach(new Consumer<Tab>() {
			@Override
			public void accept(final Tab tab) {
				final MovieWebPane webPanel = (MovieWebPane) tab.getContent();
				webPanel.load(movies, tab.isSelected());
			}
		});
	}

	private void updateTitle() {
		String main;
		String sub = "";
		switch (movies.size()) {
		case 0:
			main = NO_MOVIE_TITLE;
			break;
		case 1:
			final Movie movie = movies.iterator().next();
			main = movie.getName();
			final StringBuilder sb = new StringBuilder("  =  ");
			for(final Tag tag: movie.getTags()) {
				final String tagName = tag.getName();
				if (!tagName.equals(Tag.NO_TAG_NAME)
						&& !tagName.equals(Tag.TO_WATCH_NAME)) {
					sb.append(tagName);
					sb.append("  +  ");
				}
			}
			sub = sb.substring(0, sb.length() - 5);
			break;
		default:
			main = MULTIPLE_MOVIE_TITLE;
			break;
		}

		title.set(main, sub);
	}

	private void setDisplayMovieBox(final boolean display) {
		if (display) {
			setBottom(movieBox);
		} else {
			getChildren().remove(movieBox);
		}
	}
}
