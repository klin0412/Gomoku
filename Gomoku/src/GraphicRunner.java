import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GraphicRunner extends Application
{
	public static final int EASY = 5;
	public static final int MEDIUM = 6;
	public static final int HARD = 7;
	public static final Font DEFAULT_FONT = new Font(20);
	
	private static Stage stage;
	private static Parent root;
	private static BorderPane rootPane;
	private static Scene initScene, gameScene;
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		stage = primaryStage;
		AIWorker.setType(Stone.WHITE.type());
		stage.setTitle("Gomoku");
		stage.setResizable(false);
		root = FXMLLoader.load(this.getClass().getResource("InputScene.fxml"));
		initScene = new Scene(root, Board.WIDTH, Board.HEIGHT+60);
		rootPane = new BorderPane();
		rootPane.setCenter(Board.getInstance());
		HBox toolbar = new HBox();
		toolbar.setSpacing(10);
		toolbar.setPadding(new Insets(5, 5, 5, 5));
		rootPane.setBottom(toolbar);
		gameScene = new Scene(rootPane, Board.WIDTH, Board.HEIGHT+60);
		stage.setScene(initScene);
		stage.show();
		
		Button newGame = new Button("New Game");
		newGame.setFont(DEFAULT_FONT);
		newGame.setOnMouseClicked(e -> newGame());
	
		Button undo = new Button("Undo");
		undo.setFont(DEFAULT_FONT);
		undo.setOnMouseClicked(e ->
		{
			Board.getInstance().back();
			Board.getInstance().back();
		});
		toolbar.getChildren().addAll(newGame, undo);
	}
	
	public static void start()
	{
		ImageView front = new ImageView(root.snapshot(null, null));
    	ScaleTransition stHideFront = new ScaleTransition(Duration.millis(500), front);
    	stHideFront.setFromX(1);
    	stHideFront.setToX(0);

    	ImageView back = new ImageView(rootPane.snapshot(null, null));
    	back.setScaleX(0);

    	ScaleTransition stShowBack = new ScaleTransition(Duration.millis(500), back);
    	stShowBack.setFromX(0);
    	stShowBack.setToX(1);

    	stHideFront.setOnFinished(e -> stShowBack.play());
    	
    	StackPane pane = new StackPane();
    	pane.getChildren().addAll(front, back);
    	Scene scene = new Scene(pane, Board.WIDTH, Board.HEIGHT+60);
    	stage.setScene(scene);

    	stHideFront.play();
    	stShowBack.setOnFinished(e ->
    	{
    		stage.setScene(gameScene);
    		if(AIWorker.getType() == Stone.BLACK.type())
    			Board.getInstance().getGrid()[Board.BOARD_SIZE/2][Board.BOARD_SIZE/2].getTile().setBlack();
    	});
	}
	
	public static void newGame()
	{
		Board.getInstance().initBoard();
		ImageView front = new ImageView(rootPane.snapshot(null, null));
    	ScaleTransition stHideFront = new ScaleTransition(Duration.millis(500), front);
    	stHideFront.setFromX(1);
    	stHideFront.setToX(0);

    	ImageView back = new ImageView(root.snapshot(null, null));
    	back.setScaleX(0);

    	ScaleTransition stShowBack = new ScaleTransition(Duration.millis(500), back);
    	stShowBack.setFromX(0);
    	stShowBack.setToX(1);

    	stHideFront.setOnFinished(e -> stShowBack.play());
    	
    	StackPane pane = new StackPane();
    	pane.getChildren().addAll(front, back);
    	Scene scene = new Scene(pane, Board.WIDTH, Board.HEIGHT+60);
    	stage.setScene(scene);

    	stHideFront.play();
    	stShowBack.setOnFinished(e -> stage.setScene(initScene));
	}

	public static void main(String[] args) {launch(args);}
}
