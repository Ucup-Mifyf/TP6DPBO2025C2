import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int frameWidth = 360;
    int frameHeight = 640;

    Image backgroundImage;
    Image birdImage;
    Image lowerPipeImage;
    Image upperPipeImage;

    // player
    int playerStartPosX = frameWidth / 8;
    int playerStartPosY = frameHeight / 2;
    int playerWidth = 34;
    int playerHeight = 24;
    Player player;

    // pipes attributes
    int pipeStartPosX = frameWidth;
    int pipeStartPosY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;
    ArrayList<Pipe> pipes;

    // game logic
    Timer gameLoop;
    Timer pipesCooldown;
    int gravity = 1;
    boolean gameOver = false;
    int score = 0;
    JLabel scoreLabel;
    Font scoreFont = new Font("Arial", Font.BOLD, 24);

    // constructor
    public FlappyBird(){
        setPreferredSize(new Dimension(frameWidth, frameHeight));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);

        // load images
        backgroundImage = new ImageIcon(getClass().getResource("assets/background.png")).getImage();
        birdImage = new ImageIcon(getClass().getResource("assets/bird.png")).getImage();
        lowerPipeImage = new ImageIcon(getClass().getResource("assets/lowerPipe.png")).getImage();
        upperPipeImage = new ImageIcon(getClass().getResource("assets/upperPipe.png")).getImage();

        player = new Player(playerStartPosX, playerStartPosY, playerWidth, playerHeight, birdImage);
        pipes = new ArrayList<Pipe>();

        // Score label
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(scoreFont);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setBounds(10, 10, 200, 30);
        add(scoreLabel);

        pipesCooldown = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameOver) {
                    placePipes();
                }
            }
        });
        pipesCooldown.start();

        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }

    public void placePipes(){
        int randomPosY = (int) (pipeStartPosY - pipeHeight/4 - Math.random() * (pipeHeight/2));
        int openingSpace = frameHeight/4;

        Pipe upperPipe = new Pipe(pipeStartPosX, randomPosY, pipeWidth, pipeHeight, upperPipeImage);
        pipes.add(upperPipe);

        Pipe lowerPipe = new Pipe(pipeStartPosX, (randomPosY + openingSpace + pipeHeight), pipeWidth, pipeHeight, lowerPipeImage);
        pipes.add(lowerPipe);
    }

    public void move(){
        if (gameOver) return;

        player.setVelocityY(player.getVelocityY() + gravity);
        player.setPosY(player.getPosY() + player.getVelocityY());
        player.setPosY(Math.max(player.getPosY(), 0));

        // Check if player hit the ground
        if (player.getPosY() > frameHeight - player.getHeight()) {
            gameOver();
            return;
        }

        for (int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            pipe.setPosX(pipe.getPosX() - 2); // Move pipes to the left

            // Check collision with pipes
            if (player.getPosX() + player.getWidth() > pipe.getPosX() &&
                    player.getPosX() < pipe.getPosX() + pipe.getWidth()) {

                if (player.getPosY() < pipe.getPosY() + pipe.getHeight() &&
                        pipe.getImage() == upperPipeImage) {
                    // Collision with upper pipe
                    gameOver();
                    return;
                }

                if (player.getPosY() + player.getHeight() > pipe.getPosY() &&
                        pipe.getImage() == lowerPipeImage) {
                    // Collision with lower pipe
                    gameOver();
                    return;
                }
            }

            // Score increment when passing pipes
            if (pipe.getImage() == upperPipeImage &&
                    !pipe.passed &&
                    player.getPosX() > pipe.getPosX() + pipe.getWidth()) {
                pipe.passed = true;
                score++;
                scoreLabel.setText("Score: " + score);
            }

            // Remove pipes that are off screen
            if (pipe.getPosX() < -pipe.getWidth()) {
                pipes.remove(pipe);
                i--;
            }
        }
    }

    private void gameOver() {
        gameOver = true;
        gameLoop.stop();
        pipesCooldown.stop();

        JLabel gameOverLabel = new JLabel("GAME OVER! Press R to restart");
        gameOverLabel.setFont(scoreFont);
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setBounds(frameWidth/2 - 150, frameHeight/2 - 15, 300, 30);
        add(gameOverLabel);
        repaint();
    }

    private void restartGame() {
        gameOver = false;
        player.setPosY(playerStartPosY);
        player.setVelocityY(0);
        pipes.clear();
        score = 0;
        scoreLabel.setText("Score: " + score);
        removeAll();
        add(scoreLabel);
        gameLoop.start();
        pipesCooldown.start();
        requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e){
        move();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    @Override
    public void keyTyped(KeyEvent e){}

    @Override
    public void keyPressed(KeyEvent e){
        if (gameOver && e.getKeyCode() == KeyEvent.VK_R) {
            restartGame();
        } else if (!gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
            player.setVelocityY(-10);
        }
    }

    @Override
    public void keyReleased(KeyEvent e){}

    public void draw(Graphics g){
        g.drawImage(backgroundImage, 0, 0, frameWidth, frameHeight, null);
        g.drawImage(player.getImage(), player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight(), null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.getImage(), pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight(), null);
        }
    }
}