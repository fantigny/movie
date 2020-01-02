package net.anfoya.movie.search.javafx;

import java.net.URLStreamHandlerFactory;

import net.anfoya.java.net.cookie.PersistentCookieHandler;
import net.anfoya.java.net.filtered.easylist.EasyListRuleSet;
import net.anfoya.java.net.url.CustomHandlerFactory;
import net.anfoya.java.net.url.filter.Matcher;
import net.anfoya.java.net.url.filter.RuleSet;

public class ComponentBuilder {
	private final PersistentCookieHandler cookieHandler;
	private final RuleSet ruleSet;
	private final URLStreamHandlerFactory torrentHandlerFactory;

	private final SearchTabs searchTabs;
	private final SearchPane searchPane;

	public ComponentBuilder() {
		cookieHandler = new PersistentCookieHandler();
		ruleSet = new EasyListRuleSet(false);
		torrentHandlerFactory = new CustomHandlerFactory(new Matcher(ruleSet));

		searchTabs = new SearchTabs();
		searchPane = new SearchPane();
	}

	public PersistentCookieHandler buildCookieHandler() {
		return cookieHandler;
	}

	public SearchTabs buildSearchTabs() {
		return searchTabs;
	}

	public SearchPane buildSearchPane() {
		return searchPane;
	}

	public RuleSet buildRuleSet() {
		return ruleSet;
	}

	public URLStreamHandlerFactory buildTorrentHandlerFactory() {
		return torrentHandlerFactory;
	}
}
