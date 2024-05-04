package bricker.brick_strategies;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;

//TODO - dont forget documentation this class
public class DualBehaviorCollisionStrategy implements CollisionStrategy {
    private GameObjectCollection gameObjectCollection;
    public DualBehaviorCollisionStrategy(GameObjectCollection gameObjectCollection) {
        this.gameObjectCollection = gameObjectCollection;
    }

    @Override
    public void onCollision(GameObject thisObj, GameObject otherObj) {
        this.gameObjectCollection.removeGameObject(thisObj);
    }
}

