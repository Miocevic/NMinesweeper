package sample;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {
    Stage primarystage;
    RadioButton rbEasy;
    RadioButton rbNormal;
    RadioButton rbHard;
    Button btn_Start;
    Label winner;
    Label looser;

    private int mines_created = 0;
    private int boxSize=40;
    private int width;
    private int height;
    private int mines;
    private int opened_tiles=0;

    private int x_box;
    private int y_box;

    private int all_tiles;

    private Tile[][] grid;
    private Scene scene2;

    //This function fillBoard() is creating complet grid of rectangles on the scene
    public Parent fillBoard() {
        x_box = width / boxSize;
        y_box = height / boxSize;

        //Define number of fields
        all_tiles= x_box*y_box;

        //Creating double array of Tiles elements
        grid = new Tile[x_box][y_box];
        Pane gameScreen = new Pane();
        gameScreen.setPrefSize(width, height+50);

        //Adding fields to the Pane
        for (int y = 0; y < y_box; y++) {
            for (int x = 0; x < x_box; x++) {
                Tile tile = new Tile(x, y);
                grid[x][y] = tile;
                gameScreen.getChildren().add(tile);
            }
        }

    //Creating buttons for restarting the game
        Button btn_again = new Button("PLAY AGAIN");
        btn_again.setFont(new Font("Arial.BOLD", 14));
        btn_again.setLayoutY(height+5);
        btn_again.setLayoutX(width/2-30);
        btn_again.setPrefWidth(100);
        btn_again.setPrefHeight(40);
        gameScreen.getChildren().add(btn_again);

    //This code give btn_again code for restarting the game
        btn_again.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                scene2 = new Scene(fillBoard());
                primarystage.setScene(scene2);
                primarystage.show();
            }
        });

    //Creating and positioning winner label
        winner = new Label("YOU WIN!");
        winner.setPrefSize(width,height-10);
        winner.setTextFill(Color.DARKRED);
        winner.setFont(new Font("Arial.BOLD", 40));
        winner.setAlignment(Pos.CENTER);

     //Creating and positioning looser label
        looser = new Label("GAME OVER!");
        looser.setPrefSize(width,height-10);
        looser.setTextFill(Color.DARKRED);
        looser.setFont(new Font("Arial.BOLD", 40));
        looser.setAlignment(Pos.CENTER);

        gameScreen.getChildren().addAll(winner);
        GridPane.setHalignment(winner, HPos.CENTER);
        GridPane.setValignment(winner,VPos.CENTER);
        gameScreen.getChildren().addAll(looser);
        GridPane.setHalignment(looser, HPos.CENTER);
        GridPane.setValignment(looser, VPos.CENTER);

        winner.setVisible(false);
        looser.setVisible(false);


    // This code is randomly generate mines on fields
        for (mines_created = 0; mines_created < mines;) {

            int row = (int) (Math.random() * x_box);
            int column = (int) (Math.random() * y_box);

            if (!grid[row][column].hasMine) {
                grid[row][column].hasMine = true;
                mines_created++;
            }
        }



        // This function is discovering how much neighbours every field have and insert numbers in text field
        for (int y = 0; y < y_box; y++) {
            for (int x = 0; x < x_box; x++) {
                Tile tile = grid[x][y];

                if (tile.hasMine)
                    continue;

                int mines_neighbours = (int) getNeighbors(tile).stream().filter(t -> t.hasMine).count();

                if (mines_neighbours > 0)
                       tile.text.setText(String.valueOf(mines_neighbours));
            }
        }

        return gameScreen;
    }

    // This function is discovering how much neighbours field have
    private List<Tile> getNeighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>();

        //This is related coordinates of possible neighbours of the field
        int[] neighbour_tiles = new int[]
                {-1, -1,-1, 0,-1, 1,0, -1,0, 1,1, -1,1, 0,1, 1};

        for (int i = 0; i < neighbour_tiles.length; i++) {
            int x_move = neighbour_tiles[i];
            int y_move = neighbour_tiles[++i];

            int newX = tile.x + x_move;
            int newY = tile.y + y_move;

            if (newX >= 0 && newX < x_box && newY >= 0 && newY < y_box) {
                neighbors.add(grid[newX][newY]);
            }
        }

        return neighbors;
    }

    private class Tile extends StackPane {
        private int x, y;
        private boolean hasMine;
        private boolean isRevealed = false;

        private Rectangle box = new Rectangle(boxSize - 2, boxSize - 2);
        private Text text = new Text();

        public Tile(int x, int y) {
            this.x = x;
            this.y = y;
            this.hasMine = false;

    // This code is setting fields color, stroke and adding one to another in a grid
            box.setFill(Color.BROWN);
            box.setStroke(Color.GREY);
            box.setStrokeWidth(2);

            text.setVisible(false);

            getChildren().addAll(box, text);

            setTranslateX(x * boxSize);
            setTranslateY(y * boxSize);


    // This is events for left and right mouse click
            this.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if(opened_tiles!=(all_tiles-mines))
                    reveal_box();
                    else
                        opened_tiles=0;
                    if(this.hasMine)
                    {
                    for (int y1 = 0; y1 < y_box; y1++) {
                        for (int x1 = 0; x1 < x_box; x1++) {
                            Tile tile = grid[x1][y1];
                            opened_tiles=0;
                            tile.reveal_box();
                            }
                        }
                        looser.setVisible(true);
                    }
                }
                else if (e.getButton() == MouseButton.SECONDARY && !isRevealed) {
                    box.setFill(new ImagePattern(new Image("file:flag.png")));
                }
            });

        }

    // This function reveal_box() reveal field when you clicked on it.
    public void reveal_box() {
            if (isRevealed)
                return;

    // This code is adding mines picture to every field with a mine.
            if (this.hasMine) {
                isRevealed=true;
                box.setFill(new ImagePattern(new Image("file:bomb.png")));
                return;
            }

            isRevealed = true;
            opened_tiles++;
            text.setVisible(true);
            box.setFill(null);

    // This code is defining when the game is won
            if(!this.hasMine && (opened_tiles==(all_tiles-mines)))
                winner.setVisible(true);


    //  This code count s neighbours for empty fields (without mine or number), by calling function for every empty field!

            if (text.getText().isEmpty()) {
                getNeighbors(this).forEach(Tile::reveal_box);
            }
    //---------------------------------------------------------------

    // This part of code based on number of neighbours, replace text in fields with coresponding picture
            if (!text.getText().isEmpty()) {
                String tekst = text.getText();
                int box_text = Integer.parseInt(tekst);
                switch (box_text) {
                    case 1:
                        box.setFill(new ImagePattern(new Image("file:number1.png")));
                        break;
                    case 2:
                        box.setFill(new ImagePattern(new Image("file:number2.png")));
                        break;
                    case 3:
                        box.setFill(new ImagePattern(new Image("file:number3.png")));
                        break;
                    case 4:
                        box.setFill(new ImagePattern(new Image("file:number4.png")));
                        break;
                    case 5:
                        box.setFill(new ImagePattern(new Image("file:number5.png")));
                        break;
                    case 6:
                        box.setFill(new ImagePattern(new Image("file:number6.png")));
                        break;
                    case 7:
                        box.setFill(new ImagePattern(new Image("file:number7.png")));
                        break;
                    case 8:
                        box.setFill(new ImagePattern(new Image("file:number8.png")));
                        break;
                }
                text.setText(null);
            }
            //---------------------------------------------------------------
        }
    }


    @Override
    public void start(Stage stage) throws Exception {

    // This part of the code is creating startup screen for Application
        primarystage=stage;
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        primarystage.setTitle("NMinesweeper");
        primarystage.setScene(new Scene(gridPane, 790, 470));

        btn_Start = new Button("Start the game!");

     // This part is adding radio buttons, adding them to a ToggleGroup and placing them on gridPane.

        ToggleGroup buttonGroup = new ToggleGroup();
        rbEasy = new RadioButton("Easy (9x9, 10 mines");
        rbNormal = new RadioButton("Normal (16x16, 40 mines");
        rbHard = new RadioButton("Hard (30x16, 99 mines");

        rbEasy.setToggleGroup(buttonGroup);
        rbNormal.setToggleGroup(buttonGroup);
        rbHard.setToggleGroup(buttonGroup);

        rbEasy.setTextFill(Color.WHITE);
        rbNormal.setTextFill(Color.WHITE);
        rbHard.setTextFill(Color.WHITE);

        gridPane.setColumnIndex(rbEasy, 2);
        gridPane.setRowIndex(rbEasy, 42);

        gridPane.setColumnIndex(rbNormal, 3);
        gridPane.setRowIndex(rbNormal, 42);

        gridPane.setColumnIndex(rbHard, 4);
        gridPane.setRowIndex(rbHard, 42);

        gridPane.setColumnIndex(btn_Start, 3);
        gridPane.setRowIndex(btn_Start, 43);

    //---------------------------------------------------------------

    // This part is adding Background Image to startup screen and adding event for clicking the button.

        BackgroundImage background= new BackgroundImage(new Image("file:mine.jpg",800,480,true,true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        gridPane.setBackground(new Background(background));
        primarystage.setResizable(false);
        gridPane.getChildren().addAll(btn_Start, rbEasy, rbNormal, rbHard);

        primarystage.show();


        btn_Start.setOnAction(e->btn_Start_Click());
    //---------------------------------------------------------------
  }

    // Here is event function when button is clicked. Depending of choosen radio button
    // different parameters are set. After that we create the new scene.

    private void btn_Start_Click() {
        if (rbEasy.isSelected()){
                width = 360;
                height = 360;
                mines = 10;
            }
            else if (rbNormal.isSelected()) {
                width = 640;
                height = 640;
                mines = 40;
            } else if (rbHard.isSelected()) {
                width = 1200;
                height = 640;
                mines = 99;
            }

            if(rbEasy.isSelected() || rbNormal.isSelected() || rbHard.isSelected()) {
                scene2 = new Scene(fillBoard());
                primarystage.setScene(scene2);
                primarystage.show();
            }
    }

    //---------------------------------------------------------------

    public static void main(String[] args) {
        launch(args);
    }
}