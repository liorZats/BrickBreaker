package bricker.brick_strategies;

import bricker.gameobjects.PerkPuddle;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.util.Counter;

/**
 * This class represents a collision strategy for changing the camera state upon collision with a specific object.
 * When the specified triggering object collides with another object, it triggers a camera switch if the camera
 * switch counter is currently off.
 */
public class ChangeCameraCollisionStrategy implements CollisionStrategy{
    private static final int OFF = 0;
    private GameObjectCollection gameObjectCollection;
    private  Counter cameraSwitch;
    private  GameObject triggeringObject;

    /**
     * Constructs a ChangeCameraCollisionStrategy with the given parameters.
     *
     * @param gameObjectCollection The collection of game objects.
     * @param cameraSwitch         The counter for camera switches.
     * @param triggeringObject     The object that triggers the camera switch upon collision.
     */
    public ChangeCameraCollisionStrategy(GameObjectCollection gameObjectCollection, Counter cameraSwitch,
                                         GameObject triggeringObject) {
        this.gameObjectCollection = gameObjectCollection;
        this.cameraSwitch = cameraSwitch;
        this.triggeringObject=triggeringObject;

    }

    /**
     * Handles the collision between two game objects.
     *
     * @param thisObj  The first game object involved in the collision.
     * @param otherObj The second game object involved in the collision.
     */
    @Override
    public void onCollision(GameObject thisObj, GameObject otherObj) {
        if(otherObj.equals(triggeringObject) && this.cameraSwitch.value()==OFF)  {
            this.cameraSwitch.increment(); //switch cameraSwitch to ON
        }
        this.gameObjectCollection.removeGameObject(thisObj);
    }
}

