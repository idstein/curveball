import static java.lang.String.format;
import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.Util.checkALError;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static util.GLUtil.loadWAVSound;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALContext;
import org.lwjgl.openal.ALDevice;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

import util.GLUtil;

public class Curveball implements GLFWWindowSizeCallback.SAM,
		GLFWKeyCallback.SAM, GLFWCursorPosCallback.SAM,
		GLFWCursorEnterCallback.SAM, GLFWMouseButtonCallback.SAM {
	private GLFWErrorCallback errorCallback;

	// Rendering
	private long window;
	static final public float GLPONG_FRONT_Z = -3.0f;
	static final public float GLPONG_BACK_Z = -9.0f;
	static final public float BALL_RADIUS = 0.1f;
	final private FloatBuffer projectionFB = BufferUtils.createFloatBuffer(16);
	final private FloatBuffer modelFB = BufferUtils.createFloatBuffer(16);
	final private FloatBuffer viewFB = BufferUtils.createFloatBuffer(16);
	final private Matrix4f projection = new Matrix4f().setFrustum(-1.25f,
			1.25f, -1.25f, 1.25f, 2.5f, 9.2f).get(projectionFB);
	final private Matrix4f view = new Matrix4f().get(viewFB);
	final private Matrix4f model = new Matrix4f();

	// Input / controller
	int width = 800;
	int height = 640;
	private Vector2f mLastMousePosition;
	private boolean mMouseInside = true;

	// Game objects
	private final Ball mBall;
	private final Paddle mFrontPaddle;
	private int frontScore = 0;
	private final Paddle mBackPaddle;
	private int backScore = 0;
	private final Tube mTube;
	// Game difficulty
	private Level mCurrentLevel = Level.EASY;

	private int projectionMatrixLocation;

	private int modelMatrixLocation;

	private int pId;

	private int viewMatrixLocation;

	private int useNormalColoringLocation;

	private int useTextureLocation;

	public enum Level {
		EASY(.3f), MEDIUM(.2f), HARD(.1f), IMPOSSIBLE(.0f);

		private float handicap;

		private Level(float handicap) {
			this.handicap = handicap;
		}

		float value() {
			return handicap;
		}
	}

	/** time at last frame */
	long lastFrame;

	/** frames per second */
	int fps;
	/** last fps time */
	long lastFPS;

	private ALContext context;

	private int buzzerSoundSourceId;

	private int pingSoundSourceId;

	private int pongSoundSourceId;

	private int backgroundMusicSoundSourceId;

	private boolean playing = false;

	private Random rnd;

	private Vector3f collisionPoint;

	private int successSoundSourceId;

	private boolean paused = false;

	public int getDelta() {
		long time = GLUtil.getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;

		return delta;
	}

	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
		if (GLUtil.getTime() - lastFPS > 1000) {
			glfwSetWindowTitle(window, format("Cruveball (FPS: %d)", fps));
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	public Curveball() {
		super(); // enable gc and finalize() method
		// any exception from here will call finalize anyway...
		initGLFW();
		mTube = new Tube(2, 2, GLPONG_FRONT_Z, GLPONG_BACK_Z);
		mBall = new Ball(BALL_RADIUS, GLPONG_FRONT_Z - BALL_RADIUS - 0.01f);
		mFrontPaddle = new Paddle(true, 2f / 5f, 2f / 5f, GLPONG_FRONT_Z);
		mBackPaddle = new Paddle(false, 2f / 5f, 2f / 5f, GLPONG_BACK_Z);
		rnd = new Random();
	}

	private void initGLFW() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (glfwInit() != GL11.GL_TRUE)
			throw new IllegalStateException("Unable to initialize GLFW");

		window = createWindow(width, height);

		initInput(width, height);

		initOpenGL(width, height);

		initOpenAL();
	}

	private void initOpenAL() {
		context = ALContext.create(null, 48000, 60, false);

		buzzerSoundSourceId = loadWAVSound(getClass().getResourceAsStream(
				"/sounds/buzzer.wav"));

		backgroundMusicSoundSourceId = loadWAVSound(getClass()
				.getResourceAsStream("/sounds/sweet_success.wav"), true);

		pingSoundSourceId = loadWAVSound(getClass().getResourceAsStream(
				"/sounds/bounce.wav"));
		pongSoundSourceId = loadWAVSound(getClass().getResourceAsStream(
				"/sounds/back_bounce.wav"));

		successSoundSourceId = loadWAVSound(getClass().getResourceAsStream(
				"/sounds/ding.wav"));
	}

	private void initOpenGL(final int width, final int height) {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the ContextCapabilities instance and makes the OpenGL
		// bindings available for use.
		GLContext.createFromCurrent();

		glViewport(0, 0, width, height);

		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		/*
		 * glEnable(GL_BLEND); glBlendFunc(GL_SRC_ALPHA,
		 * GL_ONE_MINUS_SRC_ALPHA); glDepthFunc(GL_LEQUAL);
		 * glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		 */

		// Enable blending
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		// Switch to wireframe
		// glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		// -> back to solid faces:
		// glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		// Backface culling: Shows, if the triangles are correctly defined
		glDisable(GL_CULL_FACE);

		System.out.printf("OpenGL Vendor:     %s\n", glGetString(GL_VENDOR));
		System.out.printf("OpenGL Renderer:   %s\n", glGetString(GL_RENDERER));
		System.out.printf("OpenGL Version:    %s\n", glGetString(GL_VERSION));
		// System.out
		// .printf("OpenGL Extensions: %s\n", glGetString(GL_EXTENSIONS));

		initShader();
	}

	private void initShader() {
		int errorCheckValue;
		// ============================= 1. Shader: For vertices
		// ==================================
		// Load the vertex shader
		int vsId = GLUtil.loadShader(
				getClass().getResourceAsStream("/vertex.glsl"),
				GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		int fsId = GLUtil.loadShader(
				getClass().getResourceAsStream("/fragment.glsl"),
				GL20.GL_FRAGMENT_SHADER);

		// Create a new shader program that links both shaders
		pId = GL20.glCreateProgram();
		GL20.glAttachShader(pId, vsId);
		GL20.glAttachShader(pId, fsId);

		// Position information will be attribute 0
		GL20.glBindAttribLocation(pId, 0, "in_Position");
		// Color information will be attribute 1
		GL20.glBindAttribLocation(pId, 1, "in_Color");
		// Normal information will be attribute 2
		GL20.glBindAttribLocation(pId, 2, "in_Normal");
		// Texture coordinates information will be attribute 3
		GL20.glBindAttribLocation(pId, 3, "in_TextureCoord");

		GL20.glLinkProgram(pId);
		GL20.glValidateProgram(pId);

		errorCheckValue = GL11.glGetError();
		if (errorCheckValue != GL11.GL_NO_ERROR) {
			// todo: error msg
			System.out.println("ERROR - Could not create the shaders:"
					+ errorCheckValue);
			System.exit(-1);
		}

		// Get matrices uniform locations
		projectionMatrixLocation = GL20.glGetUniformLocation(pId,
				"projectionMatrix");
		viewMatrixLocation = GL20.glGetUniformLocation(pId, "viewMatrix");
		modelMatrixLocation = GL20.glGetUniformLocation(pId, "modelMatrix");

		// the switch for toggling normals als vertex colors
		useNormalColoringLocation = GL20.glGetUniformLocation(pId,
				"useNormalColoring");
		useTextureLocation = GL20.glGetUniformLocation(pId, "useTexture");
		if (errorCheckValue != GL11.GL_NO_ERROR) {
			// todo: error msg
			System.out.println("ERROR - Could get shader uniforms:"
					+ errorCheckValue);
			System.exit(-1);
		}
	}

	private void initInput(final int width, final int height) {
		mLastMousePosition = new Vector2f(width / 2, height / 2);
		// Center mouse in current window as initial position
		glfwSetCursorPos(window, mLastMousePosition.x, mLastMousePosition.y);

		// Hide cursor, when the mouse is inside the window
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

		glfwSetCursorEnterCallback(window, GLFWCursorEnterCallback(this));

		glfwSetMouseButtonCallback(window, GLFWMouseButtonCallback(this));

		// Setup a key callback. It will be called every time a key is pressed,
		// repeated or released.
		glfwSetKeyCallback(window, GLFWKeyCallback(this));

		glfwSetCursorPosCallback(window, GLFWCursorPosCallback(this));
	}

	private long createWindow(int width, int height) {
		// Configure our window
		glfwDefaultWindowHints(); // optional, the current window hints are
									// already the default
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden
												// after creation
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

		// Set OpenGL version to 3.2.0
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

		// Create the window
		long window = glfwCreateWindow(width, height, "Curveball", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Get the resolution of the primary monitor
		ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		// Center our window
		glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) - width) / 2,
				(GLFWvidmode.height(vidmode) - height) / 2);

		// Setup a window size callback for viewport adjusting while resizing
		glfwSetWindowSizeCallback(window, GLFWWindowSizeCallback(this));

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);

		return window;
	}

	enum CollisionDirection {
		LEFT, RIGHT, TOP, BOTTOM, NEAR, FAR, NONE
	}

	private void move(long millisecondsEllapsed) {
		if (!playing)
			return;
		// System.out.printf("Time ellapsed %d msecs\n", millisecondsEllapsed);
		float fracTick = millisecondsEllapsed / 1000f;

		float timeTillNextCollision = Float.POSITIVE_INFINITY;
		CollisionDirection direction = CollisionDirection.NONE;

		float timeTillLeftCollision = timeTillintersection(
				BoundingBox.LEFT_NORMAL_IN, mTube.getLeftVector(),
				mBall.getLeftVector(), mBall.getMotion());
		if (timeTillNextCollision > timeTillLeftCollision) {
			direction = CollisionDirection.LEFT;
			timeTillNextCollision = timeTillLeftCollision;
		}
		float timeTillRightCollision = timeTillintersection(
				BoundingBox.RIGHT_NORMAL_IN, mTube.getRightVector(),
				mBall.getRightVector(), mBall.getMotion());
		if (timeTillNextCollision > timeTillRightCollision) {
			direction = CollisionDirection.RIGHT;
			timeTillNextCollision = timeTillRightCollision;
		}
		float timeTillTopCollision = timeTillintersection(
				BoundingBox.TOP_NORMAL_IN, mTube.getTopVector(),
				mBall.getTopVector(), mBall.getMotion());
		if (timeTillNextCollision > timeTillTopCollision) {
			direction = CollisionDirection.TOP;
			timeTillNextCollision = timeTillTopCollision;
		}
		float timeTillBottomCollision = timeTillintersection(
				BoundingBox.BOTTOM_NORMAL_IN, mTube.getBottomVector(),
				mBall.getBottomVector(), mBall.getMotion());
		if (timeTillNextCollision > timeTillBottomCollision) {
			direction = CollisionDirection.BOTTOM;
			timeTillNextCollision = timeTillBottomCollision;
		}
		float timeTillNearCollision = timeTillintersection(
				BoundingBox.NEAR_NORMAL_IN, mTube.getNearVector(),
				mBall.getNearVector(), mBall.getMotion());
		if (timeTillNextCollision > timeTillNearCollision) {
			direction = CollisionDirection.NEAR;
			timeTillNextCollision = timeTillNearCollision;
		}
		float timeTillFarCollision = timeTillintersection(
				BoundingBox.FAR_NORMAL_IN, mTube.getFarVector(),
				mBall.getFarVector(), mBall.getMotion());
		if (timeTillNextCollision > timeTillFarCollision) {
			direction = CollisionDirection.FAR;
			timeTillNextCollision = timeTillFarCollision;
		}
		if (collisionPoint == null || mBackPaddle.hit(collisionPoint))
			mBackPaddle.getMotion().zero();

		if (fracTick < timeTillNextCollision) {
			mBall.move(fracTick);
			mBackPaddle.move(mTube, mBackPaddle.getMotion().x * fracTick,
					mBackPaddle.getMotion().y * fracTick);
		} else {
			// move till collision
			mBall.move(timeTillNextCollision);
			mBackPaddle.move(mTube, mBackPaddle.getMotion().x
					* timeTillNextCollision, mBackPaddle.getMotion().y
					* timeTillNextCollision);
			if (direction == CollisionDirection.NEAR
					&& !mFrontPaddle.hit(mBall)) {
				// Point for AI
				System.out.printf("Point for AI\n");
				backScore++;
				System.out.printf("Score %d:%d\n", frontScore, backScore);
				playScoreSound(false);
				stopMoving();
			} else if (direction == CollisionDirection.FAR
					&& !mBackPaddle.hit(mBall)) {
				// Point for player
				System.out.printf("Point for player\n");
				frontScore++;
				// increase difficulty every 5 scored points

				System.out.printf("Score %d:%d\n", frontScore, backScore);
				playScoreSound(true);
				stopMoving();
				if (frontScore % 5 == 0)
					increaseDifficulty();
			} else {
				mBall.bounce(mTube);
				updateKITargetPosition();
				playPing(direction == CollisionDirection.NEAR);
				if (direction == CollisionDirection.NEAR
						|| direction == CollisionDirection.FAR) {
					Paddle paddle = direction == CollisionDirection.NEAR ? mFrontPaddle
							: mBackPaddle;
					mBall.getMotion().add(mBall.center.x - paddle.center.x,
							mBall.center.y - paddle.center.y, 0);
				}
				mBall.move(fracTick - timeTillNextCollision);
			}
		}
	}

	private void updateKITargetPosition() {
		// AI always moves towards ball position.
		// Using ray tracing to determine future intersection point
		// http://nehe.gamedev.net/tutorial/collision_detection/17005/
		collisionPoint = intersection(new Vector3f(0, 0, 1),
				mBackPaddle.getCenter(), mBall.getCenter(), mBall.getMotion());
		if (collisionPoint == null) {
			mBackPaddle.getMotion().zero();
		} else {
			// Depending on current difficulty, it takes
			mBackPaddle
					.getMotion()
					.set(collisionPoint.mul(rnd.nextFloat() < mCurrentLevel
							.value() ? .95f : 1.f))
					.sub(mBackPaddle.getCenter());
		}
	}

	private int nextNsign() {
		return rnd.nextBoolean() ? 1 : -1;
	}

	private Vector3f intersection(Vector3f n, Vector3f v, Vector3f p,
			Vector3f dir) {
		float l2 = timeTillintersection(n, v, p, dir);
		if (l2 == Float.POSITIVE_INFINITY)
			return null;

		// ray line is not parallel to plane
		// Find Distance To Collision Point
		return new Vector3f(dir).mul(l2).add(p);
	}

	private float timeTillintersection(Vector3f n, Vector3f v, Vector3f p,
			Vector3f dir) {
		Vector3f temp = new Vector3f(v);
		float dotProduct = dir.dot(n);
		if (Math.signum(dotProduct) == 0)
			return Float.POSITIVE_INFINITY;
		float l2 = n.dot(temp.sub(p)) / dotProduct;

		if (l2 < 0)
			return Float.POSITIVE_INFINITY;

		// ray line is not parallel to plane
		// Find Distance To Collision Point
		return l2;
	}

	private void stopMoving() {
		mBall.reset();
		playing = false;
	}

	private void playScoreSound(boolean positive) {
		// play buzzer sound
		AL10.alSourcePlay(positive ? successSoundSourceId : buzzerSoundSourceId);
		checkALError();
	}

	private void playPing(boolean front) {
		// play buzzer sound
		AL10.alSourcePlay(front ? pingSoundSourceId : pongSoundSourceId);
		checkALError();
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the

		// Upload matrices to the uniform variables to shader program 0
		GL20.glUseProgram(pId);

		GL20.glUniformMatrix4fv(projectionMatrixLocation, false, projectionFB);
		GL20.glUniformMatrix4fv(viewMatrixLocation, false, viewFB);

		// Upload normal coloring and texture toggle
		GL20.glUniform1i(useNormalColoringLocation, 0);
		GL20.glUniform1i(useTextureLocation, 1);

		mBackPaddle.draw(model, modelFB, modelMatrixLocation);
		mTube.draw(model, modelFB, modelMatrixLocation);
		mBall.draw(model, modelFB, modelMatrixLocation);
		mFrontPaddle.draw(model, modelFB, modelMatrixLocation);
		GL20.glUseProgram(0);

		// Swap the color buffer. We never draw directly to the screen, only
		// in this buffer. So we need to display it
		glfwSwapBuffers(window);

		int errorCheckValue = GL11.glGetError();
		if (errorCheckValue != GL11.GL_NO_ERROR) {
			// todo: error msg
			System.out.println("ERROR - Rendering failed:" + errorCheckValue);
			System.exit(-1);
		}
	}

	public void loop() throws InterruptedException {
		// play buzzer sound
		AL10.alSourcePlay(backgroundMusicSoundSourceId);
		checkALError();

		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = GLUtil.getTime(); // call before loop to initialise fps timer
		while (glfwWindowShouldClose(window) == GL_FALSE) {
			int delta = getDelta();
			move(delta);
			render();
			updateFPS(); // update FPS Counter

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
			if (fps > 30)
				// give the CPU some rest ... This uses a threshold of a fifth
				// of the time spend
				// beyond the 30fps.
				Thread.sleep(200 / fps * (fps - 30));
		}
	}

	@Override
	protected void finalize() throws Throwable {
		ALDevice device = context.getDevice();
		context.destroy();
		device.destroy();
		glfwTerminate();
		errorCallback.release();
		super.finalize();
	}

	public void invoke(long window, int width, int height) {
		this.width = width;
		this.height = height;
		GL11.glViewport(0, 0, width, height);
	}

	public void invoke(long window, int key, int scancode, int action, int mods) {
		if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
			glfwSetWindowShouldClose(window, GL_TRUE); // We will detect
														// this in our
														// rendering
														// loop
		if (key == GLFW_KEY_D && action == GLFW_RELEASE) {
			increaseDifficulty();
		}
		if (key == GLFW_KEY_P && action == GLFW_RELEASE) {
			togglePauseGame();
		}
		if (key == GLFW_KEY_R && action == GLFW_RELEASE) {
			resetGame();
		}
	}

	public void invoke(long window, double xpos, double ypos) {
		if (!paused && mMouseInside) {
			mFrontPaddle.move(mTube, -3f
					* (float) (mLastMousePosition.x - xpos) / (float) width, 3f
					* (float) (mLastMousePosition.y - ypos) / (float) height);
		}
		mLastMousePosition.x = (float) xpos;
		mLastMousePosition.y = (float) ypos;
	}

	public void invoke(long window, int entered) {
		mMouseInside = (entered == GL_TRUE);
	}

	public void invoke(long window, int button, int action, int mods) {
		// Mouse button callback
		if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE)
			kickOffBall();
	}

	private void kickOffBall() {
		if (!playing) {
			mBall.getMotion().set(nextNsign() * rnd.nextFloat(),
					nextNsign() * rnd.nextFloat(), GLPONG_FRONT_Z);
			playing = true;
			adjustBallMotion();
		}
	}

	private void adjustBallMotion() {
		mBall.getMotion().mul(1.0f - mCurrentLevel.value());
		updateKITargetPosition();
	}

	private void increaseDifficulty() {
		Level oldLevel = mCurrentLevel;
		switch (mCurrentLevel) {
		case EASY:
			mCurrentLevel = Level.MEDIUM;
			break;
		case MEDIUM:
			mCurrentLevel = Level.HARD;
			break;
		case HARD:
			mCurrentLevel = Level.IMPOSSIBLE;
			break;
		case IMPOSSIBLE:
			break;
		}
		adjustBallMotion();
		System.out.printf("Difficulty has been increased from %s to %s\n",
				oldLevel.name(), mCurrentLevel);
	}

	private void togglePauseGame() {
		playing = !playing;
		paused = !paused;
	}

	private void resetGame() {
		stopMoving();
		frontScore = 0;
		backScore = 0;
		mCurrentLevel = Level.EASY;
	}
}
