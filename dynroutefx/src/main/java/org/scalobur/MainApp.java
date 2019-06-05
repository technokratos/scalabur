package org.scalobur;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author Denis B. Kulikov<br/>
 * date: 04.06.2019:6:03<br/>
 */
public class MainApp  extends Application {

    public static final int SIZE = 800;
    private static final int DISCRET = 100;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Rectangle rectangle1 = new Rectangle(10, 10, 20, 20);
        rectangle1.setFill(Color.BLUE);

        // Createing a green Rectangle by usage of a RectangleBuilder
        Rectangle rectangle2 = new Rectangle(100, 10, 20,20);


        // Create the VBox by usage of a VBoxBuilder
        Group group = new Group();
        VBox root = new VBox(group);

        Polygon polygon = new Polygon();
        Double[] rect = {
                0.0, 0.0,
                Double.valueOf(SIZE), 0.0,
                Double.valueOf(SIZE), Double.valueOf(SIZE),
                0.0, Double.valueOf(SIZE),
        };
        //polygon.getPoints().addAll(rect);

        Polyline polyline = new Polyline();
        polyline.getPoints().addAll(rect);
        group.getChildren().add(polyline);


        // Set the vertical spacing between children to 10px

        // Create the Scene by usage of a SceneBuilder
        Scene scene = new Scene(root);

        root.setPrefSize(SIZE, SIZE);

        // Add the scene to the Stage
        stage.setScene(scene);
        // Set the title of the Stage
        stage.setTitle("A Scene Builder Example");
        // Display the Stage
        stage.show();


        List<Node> nodes = new ArrayList<>();
        List<Node> routes = new ArrayList<>();
        RouteOptimizator routeOptimizator = new RouteOptimizator();
        NodeFactory nodeFactory = new NodeFactory(group.getChildren(), routeOptimizator);
        root.setOnMouseClicked(e-> {

            double x = e.getX();
            double y = e.getY();
            Circle circle = new Circle(x, y, SIZE/DISCRET);
            circle.setFill(Color.BLUE);
            group.getChildren().add(circle);
            if (e.isControlDown()) {
                nodeFactory.addRoute(circle);
            } else {
                nodeFactory.addNode(circle);
            }


        });
    }

    // заказ начальный конечный адрес
    //таблица весов ребер
    //маршруты




    static class Weight {
        static double[][] weights = new double[1000][1000];
    }

    static class Route {
        List<Node> nodes;
    }


    static class Edge {
        Node start, end;
    }

    @RequiredArgsConstructor
    static class Node {
        static int indexCounter = 0;
        final Circle circle;
        final int index;

        Node next;
        Node prev;

        Node originalNext;
        Node originalPrev;
        private Line nextLine;

        public Node(Circle circle) {
            this.circle = circle;
            this.index = indexCounter++;
            originalNext = null;
            originalPrev = null;

            next = null;
            prev = null;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public void setPrev(Node prev) {
            this.prev = prev;
        }

        public void setOriginalNext(Node originalNext) {
            this.originalNext = originalNext;
        }

        public void setOriginalPrev(Node originalPrev) {
            this.originalPrev = originalPrev;
        }

        static Node of (Circle circle) {
            return new Node(circle);
        }

        public void setNextLine(Line nextLine) {
            this.nextLine = nextLine;
        }

        public String toString(){
            return format("%d [%.0f;%.0f]", index, circle.getCenterX(), circle.getCenterY());
        }
    }

    private class RouteOptimizator {

    }

    private class NodeFactory {

        private final ObservableList<javafx.scene.Node> shapes;
        private final RouteOptimizator routeOptimizator;
        private final double[][] weights = new double[100][100];
        {
            for (int i = 0; i < weights.length; i++) {
                for (int j = 0; j < weights[0].length; j++) {
                    weights[i][j] = Double.MAX_VALUE;
                }

            }
        }
        private final List<Node> allNodes = new ArrayList<>();
        private final List<Node> routes = new ArrayList<>();

        public NodeFactory(ObservableList<javafx.scene.Node> shapes, RouteOptimizator routeOptimizator) {
            this.shapes = shapes;
            this.routeOptimizator = routeOptimizator;

        }

        void addNode(Circle circle) {
            Node node = Node.of(circle);
            updateWeights(node);
            process(node);
        }

        private void updateWeights(Node node) {
            allNodes.forEach(old-> {
                double weight = getWeight(node, old);
                weights[old.index][node.index] = weight;
                weights[node.index][old.index] = weight;
            });
            allNodes.add(node);
        }

        private double getWeight(Node node, Node old) {
            double x = old.circle.getCenterX();
            double y = old.circle.getCenterY();
            double nx = node.circle.getCenterX();
            double ny = node.circle.getCenterY();
            return Math.sqrt( (x-nx) * (x-nx) + (y -ny) * (y - ny));
        }

        public void process(Node node) {
            if (routes.isEmpty()) {
                routes.add(node);
            } else {
                List<Pair<Node, Double>> selectedNode = routes.stream().map(route -> getRouteAttraction(route, node)).collect(Collectors.toList());

                Optional<Pair<Node, Double>> near = selectedNode.stream().max((o1, o2) -> (int) (o1.getRight() - o2.getRight()));
                near.ifPresent(p-> addToRoute(p.getLeft(), node));


            }
        }

        private void addToRoute(Node selectedRoute, Node node) {

            Node oldNext = selectedRoute.next;

            selectedRoute.next = node;
            node.next = oldNext;
            Line line = new Line(selectedRoute.circle.getCenterX(), selectedRoute.circle.getCenterY(), node.circle.getCenterX(), node.circle.getCenterY());

            if (oldNext != null) {
                Line nextLine = new Line(node.circle.getCenterX(), node.circle.getCenterY(), oldNext.circle.getCenterX(), oldNext.circle.getCenterY());
                shapes.remove(selectedRoute.nextLine);
                node.setNextLine(nextLine);
                shapes.add(nextLine);
            }

            selectedRoute.setNextLine(line);
            shapes.add(line);


        }

        private int findMaxIndex(double[] doubles) {
            int maxIndex = 0 ;
            double maxValue = doubles[0];
            for (int i = 1; i < doubles.length; i++) {
                if (maxValue < doubles[i]) {
                    maxValue = doubles[i];
                    maxIndex = i;
                }
            }
            return maxIndex;
        }

        private Pair<Node, Double> getRouteAttraction(Node route, Node challenger) {
            Node next =  route;
            Node selectedNode = route;
            double maxValue = 0;
            do{

                double nextWeight = (next.next != null) ? weights[next.next.index][challenger.index]:0;
                double weight = weights[next.index][challenger.index] + nextWeight;

                double attractWeight = 1/weight;
                double scalarAttract = 0;
                if (next.next != null) {
                    Node nextBase = next.next;
                    Vector2 baseVector = Vector2.of(nextBase.circle.getCenterX() - next.circle.getCenterX(),
                            nextBase.circle.getCenterY() - next.circle.getCenterY());
                    Vector2 vA = Vector2.of(challenger.circle.getCenterX() - next.circle.getCenterX(),
                            challenger.circle.getCenterY() - next.circle.getCenterY());
                    Vector2 vB = Vector2.of(nextBase.circle.getCenterX() - challenger.circle.getCenterX(),
                            nextBase.circle.getCenterY() - challenger.circle.getCenterY());

                    scalarAttract = (baseVector.scalar(vA) + baseVector.scalar(vB))/ (vA.length() * vB.length());
                }

                double attract = attractWeight + 100 * scalarAttract;

                if (attract > maxValue) {
                    selectedNode = next;
                    maxValue = attract;
                }

                next = next.next;
            } while (next != null);


            return Pair.of(selectedNode, maxValue);
        }

        public void addRoute(Circle circle) {
            Node node = Node.of(circle);
            updateWeights(node);
            routes.add(node);
        }
    }

    @Data(staticConstructor = "of")
    static class Vector2 {
        final double x,y;

        double scalar(Vector2 v) {
            return x * v.x + y * v.y;
        }
        double length() {
            return Math.sqrt(x*x + y*y);
        }
    }

}
