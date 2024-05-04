package bricker.brick_strategies;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
//TODO - dont forget documentation this class
public class AddStrikeCollisionStrategy implements CollisionStrategy{
    private GameObjectCollection gameObjectCollection;
    public AddStrikeCollisionStrategy(GameObjectCollection gameObjectCollection) {
        this.gameObjectCollection = gameObjectCollection;
    }

    @Override
    public void onCollision(GameObject thisObj, GameObject otherObj) {
        gameObjectCollection.removeGameObject(thisObj);

    }
}

