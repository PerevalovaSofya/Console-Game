package org.example;


import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.System.exit;


public class Scene {

    // 0 - значение свободной клетки
    // 1 - значение препятствия в клетке
    // 2 - значение игрока
    // 3 - значение преследователей
    // 4 - значение клетки с выигрышем
    private final MatrixMy scene;
    private final List<Persecutor> persecutors;
    private final Player player;
    private final int cordHeightWin, cordWidthWin, sizeScene;

    private char emptyCellChar, wallChar, playerChar, persecutorChar, winCellChar;

    private String emptyCellColor, wallCharColor, playerCharColor, persecutorCharColor, winCellCharColor;

    private final String CONFIG, MOD;



    //функционал чтения из файла и парсинг параметров реализовать отдельно.
    private Scene(int sizeScene, int countsWall, int countsPersecutor, String mod) {
        MOD = mod;
        if (mod.equalsIgnoreCase("production")) {
            CONFIG = "/application-production.properties";
        } else {
            CONFIG = "/application-dev.properties";
        }
        this.sizeScene = sizeScene;
        scene = Generation.generationScene(sizeScene, countsWall, countsPersecutor);
        player = new Player(scene, sizeScene);
        persecutors = new ArrayList<>();
        for (int i = 0; i < countsPersecutor; i++) {
            persecutors.add(new Persecutor(scene, sizeScene));
        }
        int[] winCords = Generation.putWinPoint(scene, sizeScene);
        cordHeightWin = winCords[0];
        cordWidthWin = winCords[1];

        Generation.putWalls(scene, sizeScene, countsWall);
    }

    public static Scene getNewScene(int sizeScene, int countsWall, int countsPersecutor, String mod) {
        Scene scene = new Scene(sizeScene, countsWall, countsPersecutor, mod);
        while (!scene.findPath()) {
            scene = getNewScene(sizeScene, countsWall, countsPersecutor, mod);
        }
        scene.backEmpty();
        return scene;
    }

    private void backEmpty() {
        for (int i = 0; i < sizeScene; i++) {
            for (int j = 0; j < sizeScene; j++) {
                if (scene.getOneCell(i, j) == -1) scene.setOneCell(i, j, 0);
            }
        }
    }

    private boolean findPath() {

        if(!Generation.checkAround(scene, cordHeightWin, cordWidthWin, 2, sizeScene)) {
            return true;
        }
        int playerCordHeight = player.getCordHeight();
        int playerCordWidth = player.getCordWidth();

        if (playerCordHeight + 1 < sizeScene &&
                scene.getOneCell(playerCordHeight + 1, playerCordWidth) == 0)
            scene.setOneCell(playerCordHeight + 1, playerCordWidth, -1);

        if (playerCordWidth + 1 < sizeScene &&
                scene.getOneCell(playerCordHeight, playerCordWidth + 1) == 0)
            scene.setOneCell(playerCordHeight, playerCordWidth + 1, -1);

        if (playerCordHeight - 1 >= 0 &&
                scene.getOneCell(playerCordHeight - 1, playerCordWidth) == 0)
            scene.setOneCell(playerCordHeight - 1, playerCordWidth, -1);

        if (playerCordWidth - 1 >= 0 &&
                scene.getOneCell(playerCordHeight, playerCordWidth - 1) == 0)
            scene.setOneCell(playerCordHeight, playerCordWidth - 1, -1);


        int flagCheck = 0;
        while (check() > flagCheck) {
            flagCheck = check();
            searchValid();
            if (!Generation.checkAround(scene, cordHeightWin, cordWidthWin, -1, sizeScene))
                return true;
        }
        return false;
    }

    private int check(){
        int countPath = 0;
        for(int i = 0; i < sizeScene; i++){
            for (int j = 0; j < sizeScene; j++){
                if (scene.getOneCell(i, j) == -1)
                    countPath++;
            }
        }
        return countPath;
    }

    private void searchValid() {
        for (int y = 0; y < sizeScene; y++){
            for (int x = 0; x < sizeScene; x++){
                if(y + 1 < sizeScene && scene.getOneCell(y + 1, x) == -1 && scene.getOneCell(y, x) == 0)
                    scene.setOneCell(y, x, -1);
                if(x + 1 < sizeScene && scene.getOneCell(y, x + 1) == -1 && scene.getOneCell(y, x) == 0)
                    scene.setOneCell(y, x, -1);
                if(y - 1 > 0 && scene.getOneCell(y - 1, x) == -1 && scene.getOneCell(y, x) == 0)
                    scene.setOneCell(y, x, -1);
                if(x - 1 > 0 && scene.getOneCell(y, x - 1) == -1 && scene.getOneCell(y, x) == 0)
                    scene.setOneCell(y, x, -1);
            }
        }
    }

    public void start() {
        printScene();
        if (MOD.equalsIgnoreCase("development")) {
            System.out.println("Enter 8 for enemy step, Enter 9 for out:");
        } else {
            System.out.println("Enter 9 for out:");
        }
        Scanner scan = new Scanner(System.in);

        while (true) {
            String line = scan.nextLine();
            if (line.equalsIgnoreCase("w")) {
                if (player.getCordHeight() - 1 >= 0 && (scene.getOneCell(player.getCordHeight() - 1, player.getCordWidth()) == 0 ||
                        scene.getOneCell(player.getCordHeight() - 1, player.getCordWidth()) == 4)) {
                    if (scene.getOneCell(player.getCordHeight() - 1, player.getCordWidth()) == 4) {
                        System.out.println("You are WIN!");
                        return;
                    }
                    scene.setOneCell(player.getCordHeight(), player.getCordWidth(), 0);
                    player.setCordHeight(player.getCordHeight() - 1);
                    scene.setOneCell(player.getCordHeight(), player.getCordWidth(), 2);
                    stepPr();
                    printScene();
                }

            } else if (line.equalsIgnoreCase("d")) {
                if (player.getCordWidth() + 1 < sizeScene && (scene.getOneCell(player.getCordHeight(), player.getCordWidth() + 1) == 0 ||
                        scene.getOneCell(player.getCordHeight(), player.getCordWidth() + 1) == 4)) {
                    if (scene.getOneCell(player.getCordHeight(), player.getCordWidth() + 1) == 4) {
                        System.out.println("You are WIN!");
                        return;
                    }
                    scene.setOneCell(player.getCordHeight(), player.getCordWidth(), 0);
                    player.setCordWidth(player.getCordWidth() + 1);
                    scene.setOneCell(player.getCordHeight(), player.getCordWidth(), 2);

                    stepPr();
                    printScene();
                }

            } else if (line.equalsIgnoreCase("a")) {

                if (player.getCordWidth() - 1 >= 0 && (scene.getOneCell(player.getCordHeight(), player.getCordWidth() - 1) == 0 ||
                        scene.getOneCell(player.getCordHeight(), player.getCordWidth() - 1) == 4)) {
                    if (scene.getOneCell(player.getCordHeight(), player.getCordWidth() - 1) == 4) {
                        System.out.println("You are WIN!");
                        return;
                    }
                    scene.setOneCell(player.getCordHeight(), player.getCordWidth(), 0);
                    player.setCordWidth(player.getCordWidth() - 1);
                    scene.setOneCell(player.getCordHeight(), player.getCordWidth(), 2);

                    stepPr();
                    printScene();
                }

            } else if (line.equalsIgnoreCase("s")) {
                if (player.getCordHeight() + 1 < sizeScene && (scene.getOneCell(player.getCordHeight() + 1, player.getCordWidth()) == 0 ||
                        scene.getOneCell(player.getCordHeight() + 1, player.getCordWidth()) == 4)) {
                    if (scene.getOneCell(player.getCordHeight() + 1, player.getCordWidth()) == 4) {
                        System.out.println("You are WIN!");
                        return;
                    }
                    scene.setOneCell(player.getCordHeight(), player.getCordWidth(), 0);
                    player.setCordHeight(player.getCordHeight() + 1);
                    scene.setOneCell(player.getCordHeight(), player.getCordWidth(), 2);

                    stepPr();
                    printScene();
                }

            } else if (line.equalsIgnoreCase("9")) {
                break;
            } else if (line.equalsIgnoreCase("8") && MOD.equalsIgnoreCase("development")) {
                    stepPrDev();
            }


        }
        scan.close();
    }

    private void stepPr() {
        if (MOD.equalsIgnoreCase("production")) {
            for (int i =0; i < persecutors.size(); i++) {
                Persecutor tmp = persecutors.get(i);
                int[] rez = ChessLogic.nextStep(scene.getMatrix(), tmp.getCordHeight(), tmp.getCordWidth(),
                        player.getCordHeight(), player.getCordWidth());

                if (rez[0] == player.getCordHeight() && rez[1] == player.getCordWidth()) {
                    System.out.println("Game over!");
                    exit(0);
                }

                if (rez[0] != -1 && rez[1] != -1 && scene.getOneCell(rez[0], rez[1]) == 0) {
                    scene.setOneCell(tmp.getCordHeight(), tmp.getCordWidth(), 0);
                    tmp.setCord(rez[0], rez[1]);
                    scene.setOneCell(rez[0], rez[1], 3);
                    persecutors.set(i, tmp);
                }
            }
        }
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void stepPrDev() {
        if (MOD.equalsIgnoreCase("development")) {
            for (int i =0; i < persecutors.size(); i++) {
                Persecutor tmp = persecutors.get(i);
                int[] rez = ChessLogic.nextStep(scene.getMatrix(), tmp.getCordHeight(), tmp.getCordWidth(),
                        player.getCordHeight(), player.getCordWidth());

                if (rez[0] == player.getCordHeight() && rez[1] == player.getCordWidth()) {
                    System.out.println("Game over!");
                    exit(0);
                }

                if (rez[0] != -1 && rez[1] != -1 && scene.getOneCell(rez[0], rez[1]) == 0) {
                    scene.setOneCell(tmp.getCordHeight(), tmp.getCordWidth(), 0);
                    tmp.setCord(rez[0], rez[1]);
                    scene.setOneCell(rez[0], rez[1], 3);
                    persecutors.set(i, tmp);
                    printScene();
                    System.out.println();
                }
            }
        }
    }

    private void printScene() {
        readFromFileConfig();
        ColoredPrinter printer = new ColoredPrinter();
        for (int i = 0; i < sizeScene; i++) {
            for (int j = 0; j < sizeScene; j++) {
                if (scene.getOneCell(i, j) == 0) {
                    printer.print(emptyCellChar, Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.valueOf(emptyCellColor));
                } else if (scene.getOneCell(i, j) == 1) {
                    printer.print(wallChar, Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.valueOf(wallCharColor));
                } else if (scene.getOneCell(i, j) == 2) {
                    printer.print(playerChar, Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.valueOf(playerCharColor));
                } else if (scene.getOneCell(i, j) == 3) {
                    printer.print(persecutorChar, Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.valueOf(persecutorCharColor));
                } else if (scene.getOneCell(i, j) == 4) {
                    printer.print(winCellChar, Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.valueOf(winCellCharColor));
                }
            }
            System.out.println();
        }
    }

    private void readFromFileConfig() {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream(CONFIG))))) {
            String line;
            while (reader.ready()) {
                line = reader.readLine();
                String name = line.split("=")[0].trim();
                if (name.equalsIgnoreCase("enemy.char")) {
                    persecutorChar = line.charAt(line.length()-1);
                } else if (name.equalsIgnoreCase("player.char")) {
                    playerChar = line.charAt(line.length()-1);
                } else if (name.equalsIgnoreCase("wall.char")) {
                    wallChar = line.charAt(line.length()-1);
                } else if (name.equalsIgnoreCase("goal.char")) {
                    winCellChar = line.charAt(line.length()-1);
                } else if (name.equalsIgnoreCase("empty.char")) {
                    emptyCellChar = line.charAt(line.length()-1);
                } else if (name.equalsIgnoreCase("enemy.color")) {
                    persecutorCharColor = line.split("=")[1].trim();
                } else if (name.equalsIgnoreCase("player.color")) {
                    playerCharColor = line.split("=")[1].trim();
                } else if (name.equalsIgnoreCase("wall.color")) {
                    wallCharColor = line.split("=")[1].trim();
                } else if (name.equalsIgnoreCase("goal.color")) {
                    winCellCharColor = line.split("=")[1].trim();
                } else if (name.equalsIgnoreCase("empty.color")) {
                    emptyCellColor = line.split("=")[1].trim();
                }
            }
        } catch (Exception e) {
            persecutorChar = 'X';
            playerChar = 'o';
            wallChar = '#';
            winCellChar = 'O';
            emptyCellChar = ' ';
            persecutorCharColor = "RED";
            playerCharColor = "GREEN";
            wallCharColor = "MAGENTA";
            winCellCharColor = "BLUE";
            emptyCellColor = "YELLOW";
        }
    }
}
