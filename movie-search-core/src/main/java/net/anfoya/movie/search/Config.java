package net.anfoya.movie.search;

import net.anfoya.movie.connector.AllocineConnector;
import net.anfoya.movie.connector.ImDbConnector;
import net.anfoya.movie.connector.MovieConnector;
import net.anfoya.movie.connector.RottenTomatoesConnector;
import net.anfoya.movie.connector.SimpleMovieConnector;

public class Config {
	public MovieConnector[] getMovieConnectors() {
		return new MovieConnector[] {
				new AllocineConnector()
				, new RottenTomatoesConnector()
				, new ImDbConnector()
				, new SimpleMovieConnector(
						"YouTube"
						, "https://www.youtube.com"
						, "https://www.youtube.com/results?search_query=%s+official+trailer")
				, new SimpleMovieConnector(
						"Pirate Bay"
						, "https://thepiratebay10.org/"
						, "https://thepiratebay10.org//search/%s/1/99/0")
		};
	}
}
