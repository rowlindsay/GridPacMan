package pacman;

// ROWAN LINDSAY + NAT REDFERN
// APCS
// v0.6 -alpha

import info.gridworld.grid.*;
import info.gridworld.actor.*;
import info.gridworld.world.*;
import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;
import javax.sound.sampled.*;

public class PacManGame extends World<Actor> {
    private final PacMan PAC_MAN;
    private final Blinky BLINKY;
    private final Pinky PINKY;
    private final Inky INKY;
    private final Clyde CLYDE;
    private final GameBoard BOARD = new GameBoard(getGrid());
    private int steps;
    private String mode;
    private boolean running;
    private Grid<Actor> gr;
    
    // initializes the world
    public static void main(String[] args) {
      PacManGame game = new PacManGame();
      game.setMessage(PacMap.GAME_START_MESSAGE);
      game.show();
    }
   
   // Pre: none
   // Post: Sets up the grid, pacman, and ghosts
    public PacManGame() {
       super(new BoundedGrid<Actor>(31,28));
       running = false;
       gr = getGrid();
       Location[] scatterLocs = PacMap.GHOST_SCATTER_TARGET_LOCATIONS;
       mode = PacMap.START_MODE;
       steps = 0;
       
       PAC_MAN = new PacMan(this);
       BLINKY = new Blinky(PAC_MAN,this,scatterLocs[0]);
       PINKY = new Pinky(PAC_MAN,this,scatterLocs[1]);
       INKY = new Inky(PAC_MAN,this,BLINKY,scatterLocs[2]);
       CLYDE = new Clyde(PAC_MAN,this,scatterLocs[3]);
       
       PAC_MAN.putSelfInGrid(gr, new Location (23, 14));
       BLINKY.putSelfInGrid(gr, new Location (8, 21));
       PINKY.putSelfInGrid(gr, new Location (14,13));
       INKY.putSelfInGrid(gr, new Location (14,16));
       CLYDE.putSelfInGrid(gr, new Location (14,15));
       
       BOARD.makeBoard();
       BOARD.placeDots();
    }
    
    // Pre: none 
    // Post: rotates character depending upon key pressed
    public boolean keyPressed(String button, Location loc) {
        if(!running && button.equals(PacMap.GAME_START_BUTTON))
                startGame();
        if(running && onGrid(PAC_MAN)) {
            switch(button) {
            case PacMap.PACMAN_TURN_BUTTON_0:
                PAC_MAN.turnTo(0);
                break;
            case PacMap.PACMAN_TURN_BUTTON_180:
                 PAC_MAN.turnTo(180);
                 break;
            case PacMap.PACMAN_TURN_BUTTON_270:
                 PAC_MAN.turnTo(270);
                 break;
            case PacMap.PACMAN_TURN_BUTTON_90:
                 PAC_MAN.turnTo(90);
                 break;
            }
        }
        return true;  
    }
    
    public void startGame() {
        setMessage(PacMap.GAME_MESSAGE);
        playAudio(PacMap.INTRO_MUSIC);
        running = true;
    }
    
    // Pre: none
    // Post: revises step to move, but to make background black every move step
    public void step() {
        if(!running && onGrid(PAC_MAN))
            setMessage(PacMap.GAME_VERSION_MESSAGE + PacMap.GAME_START_BUTTON_MESSAGE);
        if(onGrid(PAC_MAN) && running) {
            ArrayList<Actor> actors = new ArrayList<Actor>();
            for (Location loc : gr.getOccupiedLocations())
                actors.add(gr.get(loc));
            for (Actor a : actors)
            {
                // only act if another actor hasn't removed a
                if (onGrid(a) && onGrid(PAC_MAN))
                    a.act();
            }
            BOARD.makeBackground(false);
            steps++;
            checkTimer();
            this.setMessage(stepMessage());
        }
        else if(running)
            endGame();
    }
    
    // Pre: none
    // Post:
    public void checkTimer() {
        if(steps >= PacMap.PHASE_LENGTH)
            switchMode();
    }
    
    public void switchMode() {
        switch(mode) {
            case PacMap.SCATTER_MODE:
                setMode(PacMap.CHASE_MODE);
                break;
            case PacMap.CHASE_MODE:
                setMode(PacMap.SCATTER_MODE);
                break;
            case PacMap.FRIGHTENED_MODE:
                setMode(PacMap.CHASE_MODE);
                break;
        }
        steps = 0;
    }
    
    public void playAudio(String dir) {
        try {
            File audioClip = new File(dir);
            AudioInputStream stream = AudioSystem.getAudioInputStream(audioClip);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.start();
        }
            catch (Exception e) {
            }
        }
    
    public boolean onGrid(Actor a)
    {
        return a.getGrid() == gr;
    }
    
    // Pre: none 
    // Post: returns mode
    public String getMode() {
        return mode;
    }
    
    public String stepMessage() {
        return PacMap.GAME_MESSAGE + '\n' + "Current Mode: " + mode
                + '\t' + '\t' + "Dots eaten: " + PAC_MAN.getDotsEaten();
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    public void endGame() {
        playAudio(PacMap.PACMAN_DEATH);
        running = false;
        System.out.println(PacMap.GAME_OVER_MESSAGE);
        this.setMessage(PacMap.GAME_OVER_MESSAGE);
    }
    
    public GameBoard getBoard() {
        return BOARD;
    }
}