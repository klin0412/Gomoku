import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GraphicRunner extends Application
{
	public static final Font defaultFont = new Font(20);
	
	@Override
	public void start(Stage primaryStage)
	{
		primaryStage.setTitle("Gomoku");
		primaryStage.setResizable(false);
		BorderPane rootPane = new BorderPane();
		rootPane.setCenter(Board.getInstance());
		VBox toolbar = new VBox();
		toolbar.setSpacing(10);
		toolbar.setPadding(new Insets(10, 10, 10, 10));
		rootPane.setRight(toolbar);
		Scene scene = new Scene(rootPane, Board.WIDTH+100, Board.HEIGHT);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		Button reset = new Button("Reset");
		reset.setFont(defaultFont);
		reset.setOnMouseClicked(e -> Board.getInstance().initBoard());
		Button black = new Button("Black");
		black.setFont(defaultFont);
		black.setOnMouseClicked(e ->
		{
			Board.getInstance().setTurn(0);
			Board.getInstance().setDTurn(2);
		});
		Button white = new Button("White");
		white.setFont(defaultFont);
		white.setOnMouseClicked(e ->
		{
			Board.getInstance().setTurn(1);
			Board.getInstance().setDTurn(2);
		});
		Button normal = new Button("Normal");
		normal.setFont(defaultFont);
		normal.setOnMouseClicked(e -> Board.getInstance().setDTurn(1));
		Button back = new Button("Back");
		back.setFont(defaultFont);
		back.setOnMouseClicked(e -> Board.getInstance().back());
		toolbar.getChildren().addAll(reset, black, white, normal, back);
	}

	public static void main(String[] args) {launch(args);}
}
