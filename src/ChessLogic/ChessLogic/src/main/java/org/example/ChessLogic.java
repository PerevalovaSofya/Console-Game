package org.example;

import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

public class ChessLogic {
    private static final int WALL = 99999;

    public static int[] nextStep(int[][] input, int stX, int stY, int fX, int fY) {
        Matrix scene = new Matrix(input, WALL);
        ArrayList<Point> path = findPath(scene, stX, stY, fX, fY);

        return new int[]{path.get(path.size() - 2).y, path.get(path.size() - 2).x};
    }

    private static ArrayList<Point> findPath(Matrix scene, int stX, int stY, int fX, int fY) {
        ArrayList<Point> way = new ArrayList<>();
        Matrix mat = wallMatrix(scene, stY, stX);


        mat.setOneCell(stY, stX, 1);
        wavePropagation(mat, stY, stX);

        mat.setOneCell(fY, fX, 55555);

//        if (mat.get(fY - 1, fX) == 0 && mat.get(fY + 1, fX) == 0
//                && mat.get(fY, fX - 1) == 0 && mat.get(fY, fX + 1) == 0)
//            return way;
        way.add(new Point(fY, fX));
        Point startPoint = new Point(stY, stX);
        while (!way.get(way.size() - 1).equals(startPoint)) {
            Point point = minNeighbourPoint(mat, way.get(way.size() - 1));
            if (!way.contains(point))
                way.add(point);
            if (way.get(way.size() - 1).x == -1) break;
        }

        return way;
    }


    private static void wavePropagation(Matrix walls, int sY, int sX) {
        int windowSize = 1;

        Stack<Point> points = new Stack<>();

        while (true) {
            for (int i = -windowSize + 1; i < windowSize; i++) {
                if (-windowSize + sY >= 0 && -windowSize + sX >= 0)
                    points.add(new Point(-windowSize + sY, -windowSize + sX));
                if (-windowSize + sY >= 0 && windowSize + sX < walls.getCol())
                    points.add(new Point(-windowSize + sY, windowSize + sX));
                if (windowSize + sY < walls.getCol() && -windowSize + sX >= 0)
                    points.add(new Point(windowSize + sY, -windowSize + sX));
                if (windowSize + sY < walls.getCol() && windowSize + sX < walls.getCol())
                    points.add(new Point(windowSize + sY, windowSize + sX));

                int len;
                Point tmpPoint = new Point(-windowSize + sY, i + sX);
                if (tmpPoint.x >= 0 && tmpPoint.x < walls.getCol() && tmpPoint.y >= 0 && tmpPoint.y < walls.getCol()) {
                    len = walls.get(tmpPoint.x + 1, tmpPoint.y);
                    if (len != 0 && len != WALL && walls.get(tmpPoint.x, tmpPoint.y) != WALL) {
                        walls.set(tmpPoint.x, tmpPoint.y, len + 1);
                    } else points.add(new Point(tmpPoint));
                }

                tmpPoint.setLocation(windowSize + sY, -i + sX);
                if (tmpPoint.x >= 0 && tmpPoint.x < walls.getCol() && tmpPoint.y >= 0 && tmpPoint.y < walls.getCol()) {
                    len = walls.get(tmpPoint.x - 1, tmpPoint.y);
                    if (len != 0 && len != WALL && walls.get(tmpPoint.x, tmpPoint.y) != WALL) {
                        walls.set(tmpPoint.x, tmpPoint.y, len + 1);
                    } else points.add(new Point(tmpPoint));
                }

                tmpPoint.setLocation(i + sY, windowSize + sX);
                if (tmpPoint.x >= 0 && tmpPoint.x < walls.getCol() && tmpPoint.y >= 0 && tmpPoint.y < walls.getCol()) {
                    len = walls.get(tmpPoint.x, tmpPoint.y - 1);
                    if (len != 0 && len != WALL && walls.get(tmpPoint.x, tmpPoint.y) != WALL) {
                        walls.set(tmpPoint.x, tmpPoint.y, len + 1);
                    } else points.add(new Point(tmpPoint));
                }

                tmpPoint.setLocation(-i + sY, -windowSize + sX);
                if (tmpPoint.x >= 0 && tmpPoint.x < walls.getCol() && tmpPoint.y >= 0 && tmpPoint.y < walls.getCol()) {
                    len = walls.get(tmpPoint.x, tmpPoint.y + 1);
                    if (len != 0 && len != WALL && walls.get(tmpPoint.x, tmpPoint.y) != WALL) {
                        walls.set(tmpPoint.x, tmpPoint.y, len + 1);
                    } else points.add(new Point(tmpPoint));
                }
                fillEmptyPoints(walls, points);
            }
            points.clear();


            if (windowSize + sY >= walls.getRow() && windowSize + sX >= walls.getRow()
                    && -windowSize + sY < 0 && -windowSize + sX < 0)
                break;
            windowSize++;
        }
    }

    private static Matrix wallMatrix(Matrix scene, int sY, int sX) {
        Matrix walls = new Matrix(scene.getRow(), scene.getCol(), WALL);
        for (int i = 0; i < walls.getRow(); i++)
            for (int j = 0; j < scene.getCol(); j++)
                if (scene.get(i, j) == 3 || scene.get(i, j) == 1 || scene.get(i, j) == 4) {
                    walls.setOneCell(i, j, WALL);
                } else if (scene.get(i, j) == 2) {
                    walls.setOneCell(i, j, 55555);
                } else {
                    walls.setOneCell(i, j, 0);
                }
        walls.setOneCell(sY, sX, 0);
        return walls;
    }

    private static Point minNeighbourPoint(Matrix walls, Point cord) {
        Point point = new Point(-1, -1);
        int min = WALL;
        int tmp = walls.get(cord.x - 1, cord.y);
        if (tmp != 0 && tmp != WALL && tmp < min) {
            min = tmp;
            point.setLocation(cord.x - 1, cord.y);
        }
        tmp = walls.get(cord.x + 1, cord.y);
        if (tmp != 0 && tmp != WALL && tmp < min) {
            min = tmp;
            point.setLocation(cord.x + 1, cord.y);
        }
        tmp = walls.get(cord.x, cord.y - 1);
        if (tmp != 0 && tmp != WALL && tmp < min) {
            min = tmp;
            point.setLocation(cord.x, cord.y - 1);
        }
        tmp = walls.get(cord.x, cord.y + 1);
        if (tmp != 0 && tmp != WALL && tmp < min) {
            point.setLocation(cord.x, cord.y + 1);
        }
        return point;
    }

    private static int minNeighbourValue(Matrix walls, Point cord) {
        int min = WALL;
        int tmp = walls.get(cord.x - 1, cord.y);
        if (tmp != 0 && tmp != WALL && tmp < min) min = tmp;

        tmp = walls.get(cord.x + 1, cord.y);
        if (tmp != 0 && tmp != WALL && tmp < min) min = tmp;

        tmp = walls.get(cord.x, cord.y - 1);
        if (tmp != 0 && tmp != WALL && tmp < min) min = tmp;

        tmp = walls.get(cord.x, cord.y + 1);
        if (tmp != 0 && tmp != WALL && tmp < min) min = tmp;

        return min;
    }

    private static void fillEmptyPoints(Matrix walls, Stack<Point> points) {
        while (!points.empty()) {
            if (walls.get(points.peek().x, points.peek().y) < WALL)
                walls.set(points.peek().x, points.peek().y, 1 + minNeighbourValue(walls, points.peek()));
            points.pop();
        }
    }
}