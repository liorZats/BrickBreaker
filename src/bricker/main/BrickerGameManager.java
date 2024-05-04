package bricker.main;

import bricker.brick_strategies.*;
import bricker.gameobjects.*;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.*;
import danogl.gui.rendering.Camera;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Counter;
import danogl.util.Vector2;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * Manages the game logic and objects for the Joni&Lior bricker.IO game.
 */
public class BrickerGameManager extends GameManager {
    private static final int ON = 1;
    private static final int OFF = 0;
    private static final float THICKNESS_WALL = 10;
    private static final float PADDLE_SPEED = 700;
    private static final float BALL_VELOCITY = 300;
    private static final float THICKNESS_BRICK = 15;
    private static final Vector2 PADDLE_SIZE_VEC = new Vector2(100, 15);
    private static final Vector2 BALL_SIZE_VEC = new Vector2(20, 20);
    private static final int DEFAULT_NUM_BRICK_IN_ROW = 7;
    private static final int DEFAULT_NUM_ROWS_OF_BRICKS = 8;
    private static final int STRIKES = 3;
    private static final int PERK_PUDDLE_EXPIRATION = 4;

    private Vector2 windowDimensions;
    private int bricksInRow;
    private int rowsOfBricks;
    private WindowController windowController;
    private UserInputListener inputListener;
    private Ball ball;
    private Counter strikesCount;
    private Counter cameraSwitch;
    private Counter bricksCount;
    private int cameraCount;
    private Counter perkPaddleState; //0: perk paddle doesn't exit. 1+i: exists with i collisions. (0<i<=4)

    /**
     * Constructs a BrickerGameManager instance with the specified window title, window dimensions, number of bricks in a row, and number of rows of bricks.
     *
     * @param windowTitle      The title of the game window.
     * @param windowDimensions The dimensions of the game window.
     * @param bricksInRow      The number of bricks in a row.
     * @param rowsOfBricks     The number of rows of bricks.
     */

    public BrickerGameManager(String windowTitle, Vector2 windowDimensions, int bricksInRow, int rowsOfBricks) {
        super(windowTitle, windowDimensions);
        this.windowDimensions = windowDimensions;
        this.bricksInRow = bricksInRow;
        this.rowsOfBricks = rowsOfBricks;
    }

    /**
     * Creates a perk puddle object.
     *
     * @param imageReader   The image reader object used to load the perk puddle image.
     * @param soundReader   The sound reader object.
     * @param inputListener The user input listener object for controlling the perk puddle.
     * @return A perk puddle object.
     */
    private PerkPuddle createPerkPuddle(ImageReader imageReader, SoundReader soundReader, UserInputListener inputListener) {
        Renderable paddle = imageReader.readImage("assets/paddle.png", true);
        PerkPuddle perkPaddle = new PerkPuddle(Vector2.ZERO, PADDLE_SIZE_VEC, paddle, inputListener, PADDLE_SPEED, this.perkPaddleState);
        Vector2 location = new Vector2(this.windowDimensions.x() / 2, windowDimensions.y() / 2);
        perkPaddle.setCenter(location);
        return perkPaddle;
    }

    /**
     * Creates a puck object.
     *
     * @param imageReader The image reader object used to load the puck image.
     * @param soundReader The sound reader object used to load the collision sound for the puck.
     * @return A puck object.
     */
    private Ball createPuck(ImageReader imageReader, SoundReader soundReader) {
        Renderable puckImage = imageReader.readImage("assets/mockBall.png", true);
        Sound collisionSound = soundReader.readSound("assets/Bubble5_4.wav");
        Ball puck = new Ball(Vector2.ZERO, BALL_SIZE_VEC.mult(0.75f), puckImage, collisionSound); //Todo: maybe change this to puck object?
        placeBall(windowDimensions.mult(0.5f), puck);
        return puck;
    }

    /**
     * Adds a ball to the game area.
     *
     * @param imageReader The image reader object used to load the ball image.
     * @param soundReader The sound reader object used to load the collision sound for the ball.
     */
    private void addBall(ImageReader imageReader, SoundReader soundReader) {
        Renderable ballImage = imageReader.readImage("assets/ball.png", true);
        Sound collisionSound = soundReader.readSound("assets/Bubble5_4.wav");
        this.ball = new Ball(Vector2.ZERO, BALL_SIZE_VEC, ballImage, collisionSound);
        placeBall(windowDimensions.mult(0.5f), this.ball);
        gameObjects().addGameObject(this.ball);
    }

    /**
     * Adds a user-controlled paddle to the game area.
     *
     * @param imageReader   The image reader object used to load the paddle image.
     * @param inputListener The user input listener object for controlling the paddle.
     */
    private void addPaddle(ImageReader imageReader, UserInputListener inputListener) {
        Renderable paddleImage = imageReader.readImage("assets/paddle.png", true);
        GameObject userPaddle = new UserPaddle(Vector2.ZERO, PADDLE_SIZE_VEC, paddleImage, inputListener, PADDLE_SPEED);
        Vector2 initialPosPaddle = new Vector2(this.windowDimensions.x() / 2, windowDimensions.y() - 30);
        userPaddle.setCenter(initialPosPaddle);
        gameObjects().addGameObject(userPaddle);
    }


    /**
     * Adds bricks to the game area.
     *
     * @param imageReader   The image reader object used to load the brick image.
     * @param bricksInRow   The number of bricks in each row.
     * @param rowsOfBricks  The number of rows of bricks.
     * @param soundReader   The sound reader object.
     * @param inputListener The user input listener object.
     */
    private void addBricks(ImageReader imageReader, float bricksInRow, float rowsOfBricks, SoundReader soundReader,
                           UserInputListener inputListener) {
        Renderable brickImage = imageReader.readImage("assets/brick.png", true);
        float sizeBrickVecX = (int) ((windowDimensions.x() - 2 * (THICKNESS_WALL)) / bricksInRow);
        float sizeBrickVecY = THICKNESS_BRICK;
        Vector2 sizeBrickVec = new Vector2(sizeBrickVecX, sizeBrickVecY);

        for (int i = 0; i < rowsOfBricks; i++) {
            for (int j = 0; j < bricksInRow; j++) {
                float locationX = THICKNESS_WALL + j * sizeBrickVec.x();  // Calculate x-coordinate
                float locationY = THICKNESS_WALL + i * THICKNESS_BRICK;  // Calculate y-coordinate

                CollisionStrategy collisionStrategy = chooseCollisionStrategy(imageReader, soundReader, inputListener);
                //Todo Perhaps we should consider extending the input arguments of the chooseCollisionStrategy method
                // according to the implementation of the others strategies.

                GameObject brick = new Brick(new Vector2(locationX, locationY), sizeBrickVec, brickImage,
                        collisionStrategy, bricksCount);
                gameObjects().addGameObject(brick);
            }
        }
    }

    /**
     * Adds a background image to the game.
     *
     * @param imageReader The image reader object used to load the background image.
     */
    private void addBackground(ImageReader imageReader) {
        // Full background
        Renderable backgroundImage = imageReader.readImage("assets/DARK_BG2_small.jpeg",
                false);
        GameObject background = new GameObject(Vector2.ZERO, windowDimensions, backgroundImage);
        gameObjects().addGameObject(background, Layer.BACKGROUND);
    }

    /**
     * Adds walls to the game area.
     * Four walls are added: top, left, right, and bottom.
     *
     * @param windowDimensionsX The width of the game window.
     * @param windowDimensionsY The height of the game window.
     * @param thickness         The thickness of the walls.
     * @param wallColor         The color of the walls.
     */
    private void addWalls(float windowDimensionsX, float windowDimensionsY, float thickness, Color wallColor) {
        RectangleRenderable color = new RectangleRenderable(wallColor);
        // top wall
        GameObject topWall = new GameObject(Vector2.ZERO, new Vector2(windowDimensionsX, thickness), color);
        topWall.setCenter(new Vector2(windowDimensionsX / 2, 0));
        gameObjects().addGameObject(topWall);
        // left wall
        GameObject leftWall = new GameObject(Vector2.ZERO, new Vector2(thickness, windowDimensionsY), color);
        leftWall.setCenter(new Vector2(0, windowDimensionsY / 2));
        gameObjects().addGameObject(leftWall);
        // right wall
        GameObject rightWall = new GameObject(Vector2.ZERO, new Vector2(thickness, windowDimensionsY), color);
        rightWall.setCenter(new Vector2(windowDimensionsX, windowDimensionsY / 2));
        gameObjects().addGameObject(rightWall);
        // bottom wall background
        GameObject botWall = new GameObject(Vector2.ZERO, new Vector2(windowDimensionsX, thickness), color);
        botWall.setCenter(new Vector2(windowDimensionsX / 2, windowDimensionsY));
        gameObjects().addGameObject(botWall, Layer.BACKGROUND);
    }

    /**
     * Places the ball at the specified center with a random initial velocity.
     *
     * @param centerVec  The center vector where the ball should be placed.
     * @param ballObject The ball object to be placed.
     */
    private void placeBall(Vector2 centerVec, Ball ballObject) {
        ballObject.setVelocity(Vector2.DOWN.mult(BALL_VELOCITY));
        ballObject.setCenter(centerVec);

        //Generates random initial velocity direction for a ball
        Random rand = new Random();
        double angle = rand.nextDouble() * Math.PI;
        float ballVelX = (float) Math.cos(angle) * BALL_VELOCITY;
        float ballVelY = (float) Math.sin(angle) * BALL_VELOCITY;
        ballObject.setVelocity(new Vector2(ballVelX, ballVelY));
    }

    /**
     * Keeps the user paddle and balls in bounds.
     * If the user paddle goes out of bounds, it moves it back into bounds.
     * If a ball goes out of bounds, it removes it from the game.
     */
    private void checkBounds() {
        /*
         * Keeps the userPaddle in bounds.
         */
        float leftBounds = this.windowDimensions.x() - THICKNESS_WALL;
        float rightBounds = THICKNESS_BRICK;
        float upperBounds = THICKNESS_BRICK;
        float bottomBounds = windowDimensions.y() - THICKNESS_WALL;

        for (GameObject gameObject : gameObjects()) {
            if (gameObject instanceof UserPaddle) {
                if (gameObject.getTopLeftCorner().x() < rightBounds) {
                    Vector2 movementBackLeft = new Vector2(PADDLE_SPEED / 100, 0);
                    gameObject.setTopLeftCorner(gameObject.getTopLeftCorner().add(movementBackLeft));
                }
                if (gameObject.getTopLeftCorner().x() + PADDLE_SIZE_VEC.x() > leftBounds) {
                    Vector2 movementBackRight = new Vector2(-1 * PADDLE_SPEED / 100, 0);
                    gameObject.setTopLeftCorner(gameObject.getTopLeftCorner().add(movementBackRight));
                }
            }
            if (gameObject instanceof Ball && !gameObject.equals(this.ball) && gameObject.getTopLeftCorner().y() > bottomBounds)
                gameObjects().removeGameObject(gameObject);
        }
    }

    /**
     * Checks and manages the state of the camera based on specific conditions.
     * If the camera switch is ON and the camera count is 0, a new camera is set
     * to follow the ball. The camera count is updated to the ball's collision counter.
     * If the difference between the ball's collision counter and the camera count is 4,
     * the camera is set to null, camera count is reset to 0, and the camera switch is decremented.
     */

    private void checkCameraState() {
        if (this.cameraSwitch.value() == ON && this.cameraCount == 0) {
            setCamera(new Camera(this.ball, Vector2.ZERO, this.windowDimensions.mult(1.2f),
                    this.windowDimensions));
            this.cameraCount = this.ball.getCollisionCounter();
            return;
        }
        if (this.ball.getCollisionCounter() - cameraCount == 4) {
            setCamera(null);
            this.cameraCount = 0;
            this.cameraSwitch.decrement();
        }
    }

    private void checkPerkPaddleState() {
        if (perkPaddleState.value() == PERK_PUDDLE_EXPIRATION) {
            for (GameObject gameObject : gameObjects()) {
                if (gameObject instanceof PerkPuddle) {
                    this.gameObjects().removeGameObject(gameObject);
                    this.perkPaddleState.reset();
                }
            }
        }
    }

    /**
     * Checks and manages the state of the perk puddle.
     * If the perk puddle state indicates expiration, it removes all perk puddle game objects
     * from the game and resets the perk paddle state.
     */
    private void checkForGameEnd() {
        double ballHeight = this.ball.getCenter().y();
        // Ball reached beneath user's paddle
        if (ballHeight > windowDimensions.y() - THICKNESS_WALL) {
            this.strikesCount.decrement(); // -1 life
            if (this.strikesCount.value() == 0) {
                endGame("You've lost ");
            } else
                placeBall(windowDimensions.mult(0.5f), this.ball);
        }
        // Broke all Bricks indicates a win
        if (bricksCount.value() == 0 || this.inputListener.isKeyPressed(KeyEvent.VK_W)) {
            endGame("You've won ");
        }
    }

    /**
     * Ends the game in win or lose states.
     * It prompts the user with the given prompt to play again.
     * If the user chooses to play again, it resets the game using the window controller.
     * If the user chooses not to play again, it closes the game window.
     *
     * @param prompt The prompt to display to the user.
     */
    private void endGame(String prompt) {
        prompt += "Play again?";
        if (windowController.openYesNoDialog(prompt))
            windowController.resetGame();
        else
            windowController.closeWindow();
    }

    /**
     * Chooses a collision strategy based on a random number.
     * The collision strategy determines how collisions between game objects are handled.
     *
     * @param imageReader   The image reader object.
     * @param soundReader   The sound reader object.
     * @param inputListener The user input listener object.
     * @return A collision strategy to be used in the game.
     */
    private CollisionStrategy chooseCollisionStrategy(ImageReader imageReader, SoundReader soundReader,
                                                      UserInputListener inputListener) {
        Random rand = new Random();
        CollisionStrategy collisionStrategy;
        double randomNumber = rand.nextDouble();
        int caseNumber = (int) (randomNumber * 10);
        switch (caseNumber) {
            case 0:
                Ball puck1 = createPuck(imageReader, soundReader);
                Ball puck2 = createPuck(imageReader, soundReader);
                collisionStrategy = new NewPucksCollisionStrategy(gameObjects(), puck1, puck2);
                break;
            case 1:
                PerkPuddle perkPuddle = createPerkPuddle(imageReader, soundReader, inputListener);
                collisionStrategy = new PerkPuddleCollisionStrategy(gameObjects(), perkPuddle, this.perkPaddleState);
                break;
            case 2:
                collisionStrategy = new ChangeCameraCollisionStrategy(gameObjects(), this.cameraSwitch, this.ball);
                break;
            case 3: //TODO: should implement AddStrikeCollisionStrategy
                collisionStrategy = new AddStrikeCollisionStrategy(gameObjects());
                break;
            case 4: //TODO: should implement DualBehaviorCollisionStrategy
                collisionStrategy = new DualBehaviorCollisionStrategy(gameObjects());
                break;
            default: //0.5 percentages change to get BasicCollisionStrategy
                collisionStrategy = new BasicCollisionStrategy(gameObjects());
                break;
        }
        return collisionStrategy;
    }

    /**
     * Generates a graphic display showing the number of strikes left using heart images.
     * The number of heart images displayed represents the number of strikes left.
     * The hearts are rendered horizontally and scaled based on the number of strikes.
     * The display is added to the game objects to be rendered.
     *
     * @param imageReader The image reader object used to load the heart image.
     */
    private void generateGraphicStrikesDisplay(ImageReader imageReader) {
        Renderable heartImage = imageReader.readImage("assets/heart.png", true);
        // calculation the dimensions of the heart
        float heartWidth = (int) ((windowDimensions.x() / 6) / STRIKES);
        float heartHeight = (int) ((windowDimensions.y() / 6) / STRIKES);
        Vector2 heartDim = new Vector2(heartWidth, heartHeight);
        Vector2 strikesGraphicDim = new Vector2(heartWidth * STRIKES, heartHeight);
        StrikesGraphic strikesGraphic = new StrikesGraphic(strikesGraphicDim, this.windowDimensions, heartDim, heartImage, gameObjects(), this.strikesCount);
        gameObjects().addGameObject(strikesGraphic);
    }

    /**
     * Generates a numeric display showing the number of strikes left.
     * The display is rendered as "Strikes left: X", where X is the number of strikes.
     * The display is added to the game objects to be rendered on the UI layer.
     */
    private void generateNumericStrikesDisplay() {
        // Create a text renderable with the current number of strikes
        TextRenderable displayNumber = new TextRenderable(String.format("Strikes left: %d", STRIKES));
        // Define the dimensions and location of the numeric display
        Vector2 strikesNumericDim = new Vector2(20, 20);
        Vector2 strikesNumericLocation = new Vector2(10, this.windowDimensions.y() - 100);
        // Create a StrikesNumeric object to represent the numeric display
        StrikesNumeric strikesNumeric = new StrikesNumeric(strikesNumericLocation, strikesNumericDim, displayNumber, gameObjects(), this.strikesCount);
        gameObjects().addGameObject(strikesNumeric, Layer.UI);
    }


    /**
     * Initializes the game by creating game objects, counters, and setting up the game environment.
     *
     * @param imageReader      The image reader used to load images for game objects.
     * @param soundReader      The sound reader used to load sounds for game objects.
     * @param inputListener    The input listener used to handle user input.
     * @param windowController The window controller used to manage the game window.
     */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        //Creates game objects:
        this.strikesCount = new Counter(STRIKES);
        this.bricksCount = new Counter(0);
        this.perkPaddleState = new Counter(0);
        this.cameraSwitch = new Counter(OFF);
        this.cameraCount = 0;
        this.windowController = windowController;
        this.inputListener = inputListener;
        addBall(imageReader, soundReader);
        addPaddle(imageReader, inputListener);
        addBricks(imageReader, this.bricksInRow, this.rowsOfBricks, soundReader, inputListener);
        addBackground(imageReader);
        generateGraphicStrikesDisplay(imageReader);
        generateNumericStrikesDisplay();
        addWalls(windowDimensions.x(), windowDimensions.y(), THICKNESS_WALL, Color.BLACK);
    }

    /**
     * Updates the game state for the current frame.
     *
     * @param deltaTime The time elapsed, in seconds, since the last frame.
     *                  Can be used to determine changes in game state based on time.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        checkBounds(); // Keeps userPaddle in bounds
        checkPerkPaddleState();
        checkCameraState();
        checkForGameEnd();

    }

    /**
     * The entry point of the application.
     *
     * @param args Command-line arguments. The first argument is the number of bricks in a row,
     *             and the second argument is the number of rows of bricks. If only one argument
     *             is provided, it's assumed to be the number of rows of bricks.
     */
    public static void main(String[] args) {
        int bricksInRow = DEFAULT_NUM_BRICK_IN_ROW;
        int rowsOfBricks = DEFAULT_NUM_ROWS_OF_BRICKS;
        if (args.length >= 2) {
            bricksInRow = Integer.parseInt(args[0]);
            rowsOfBricks = Integer.parseInt(args[1]);
        }
        if (args.length == 1) {
            rowsOfBricks = Integer.parseInt(args[0]);
        }
        new BrickerGameManager("Joni&Lior bricker.io", new Vector2(700, 500), bricksInRow, rowsOfBricks).run();
    }

}
