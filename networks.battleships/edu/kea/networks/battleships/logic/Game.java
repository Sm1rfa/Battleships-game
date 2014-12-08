package edu.kea.networks.battleships.logic;

import java.util.Random;
/**
 * Class which holds the logic of the game.
 * Not completely working at this moment.
 * @author Stoyan Bonchev
 *
 */
public class Game {

	int[][] myBoard = new int[10][10];
	int[][] enemyBoard = new int[10][10];
    int[][] ships = new int[3][2];
    int[] shoot = new int[2];
    int shotHit = 0;
    
    public void putShips(int[][] ships) {
    	Random random = new Random();
    	for(int ship = 0; ship <= 3; ship++) {
    		ships[ship][0] = random.nextInt(5);
    		ships[ship][1] = random.nextInt(5);
    		
            for(int last = 0; last < ship; last++){
                if((ships[ship][0] == ships[last][0]) && (ships[ship][1] == ships[last][1]))
                    do {
                        ships[ship][0] = random.nextInt(5);
                        ships[ship][1] = random.nextInt(5);
                    } while( (ships[ship][0] == ships[last][0])&&(ships[ship][1] == ships[last][1]));
            }
    	}
    }
    
    public static boolean hit(int[] shoot, int[][] ships) {
        
        for(int ship = 0 ; ship < ships.length; ship++){
            if( shoot[0] == ships[ship][0] && shoot[1] == ships[ship][1]) {
                return true;
            }
        }
        return false;
    }
}
