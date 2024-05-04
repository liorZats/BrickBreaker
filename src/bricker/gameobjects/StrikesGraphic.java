package bricker.gameobjects;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.rendering.Renderable;
import danogl.util.Counter;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;

/**
 * Responsible for displaying the game's strikes graphically with the supplied Renderable.
 */
public class StrikesGraphic extends GameObject {
    private Counter strikesLeft; // Points to the main Counter object managed by BrickerGameManager
    private int symbolsCount; // Local counter
    private GameObjectCollection gameObjectCollection;
    private GameObject[] strikeSymbols;

    /**
     * Constructs a new StrikesGraphic instance.
     *
     * @param strikesGraphicDimensions Dimensions of the strikes graphic.
     * @param screenDimensions         Dimensions of the game screen.
     * @param renderableDimensions     Dimensions of the renderable representing the strike symbols.
     * @param renderable               The renderable representing the strike symbols.
     * @param gameObjectCollection     The collection of game objects.
     * @param initialStrikes           The initial number of strikes.
     */
    public StrikesGraphic(Vector2 strikesGraphicDimensions,Vector2 screenDimensions, Vector2 renderableDimensions, Renderable renderable,
                          GameObjectCollection gameObjectCollection, Counter initialStrikes) {
        super(Vector2.ZERO, Vector2.ZERO, renderable);
        this.strikesLeft = initialStrikes;
        this.symbolsCount = initialStrikes.value();
        this.gameObjectCollection = gameObjectCollection;
        this.strikeSymbols = new GraphicStrike[symbolsCount];
        for (int i = 0; i < symbolsCount; i++) {
            float locationX =  i * strikesGraphicDimensions.x()/symbolsCount + 20;  // Calculate x-coordinate
            float locationY = screenDimensions.y() - 50; // Calculate y-coordinate
            GraphicStrike graphicStrike = new GraphicStrike(new Vector2(locationX, locationY), renderableDimensions, renderable);
            this.strikeSymbols[i] = graphicStrike;
            gameObjectCollection.addGameObject(graphicStrike, Layer.UI);
        }
    }

    /**
     * Updates the StrikesGraphic instance.
     *
     * @param deltaTime The time elapsed since the last update.
     */
    public void update(float deltaTime) {
        super.update(deltaTime);
        //TODO:: < or >
        if(strikesLeft.value() != symbolsCount){
            this.gameObjectCollection.removeGameObject(strikeSymbols[symbolsCount - 1], Layer.UI);
            symbolsCount--;
        }
    }
}
