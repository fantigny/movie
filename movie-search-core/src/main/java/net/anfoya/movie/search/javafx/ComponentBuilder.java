package net.anfoya.movie.search.javafx;

public class ComponentBuilder {
	private final SearchTabs searchTabs;
	private final SearchPane searchPane;

	public ComponentBuilder() {
		searchTabs = new SearchTabs();
		searchPane = new SearchPane();
	}

	public SearchTabs buildSearchTabs() {
		return searchTabs;
	}

	public SearchPane buildSearchPane() {
		return searchPane;
	}
}
