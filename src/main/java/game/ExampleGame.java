package game;

import java.io.FileInputStream;
import java.util.Properties;

import core.DragonEngine;

final class ExampleGame
{
	public static void main(final String[] args)
	{
		try
		{
			final Properties gameProperties = new Properties();
			gameProperties.load(new FileInputStream("src/main/resources/game.properties"));

			DragonEngine.getInstance().start(new GameLogic(gameProperties), gameProperties);
		}
		catch(final Exception e)
		{
			//TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}