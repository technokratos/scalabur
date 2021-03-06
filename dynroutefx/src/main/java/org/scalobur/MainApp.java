package org.scalobur;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author Denis B. Kulikov<br/>
 * date: 04.06.2019:6:03<br/>
 */
public class MainApp extends Application {

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
        Rectangle rectangle2 = new Rectangle(100, 10, 20, 20);


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
        NodeFactory nodeFactory = new NodeFactory(group.getChildren());
        AtomicReference<Node> prevNode = new AtomicReference<>(null);
        root.setOnMouseClicked(e -> {

            double x = e.getX();
            double y = e.getY();
            Circle circle = new Circle(x, y, SIZE / DISCRET);

            group.getChildren().add(circle);


            Node node;
            if (e.isControlDown()) {

                node = nodeFactory.addRoute(new Position(circle));
            } else {


                if (prevNode.get() != null) {
                    node = nodeFactory.initOnlyNode(new Position(circle));
                    prevNode.get().originalNext = node;
                    nodeFactory.process(prevNode.get());
                    prevNode.set(null);
                } else {

                    if (!e.isShiftDown()) {//add route

                        if (prevNode.get() == null) {
                            node = nodeFactory.addNode(new Position(circle));
                        } else {
                            node = nodeFactory.initOnlyNode(new Position(circle));
                            prevNode.get().originalNext = node;
                            nodeFactory.process(prevNode.get());
                            prevNode.set(null);
                        }

                    } else {//if pressed make original
                        node = nodeFactory.initOnlyNode(new Position(circle));
                        if (prevNode.get() == null) {
                            circle.setFill(nodeFactory.getRandomColor());
                            prevNode.set(node);
                        } else {
                            prevNode.get().originalNext = node;
                            node.markAs(prevNode.get());;
                        }
                    }
                }
            }


        });

    }

    @AllArgsConstructor
    static class SingleInsertInfo {
        Node route;
        Node insert;
        Node challenger;
        Double newLen;


    }



    @RequiredArgsConstructor
    static class Node {
        static int indexCounter = 0;
        final Position circle;

        final int index;

        Node next;
        Node prev;

        Node originalNext;
        Node originalPrev;
        private Line nextLine;

        public Node(Position circle) {
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

        static Node of(Position circle) {
            return new Node(circle);
        }


        public void setNextLine(Line nextLine) {
            this.nextLine = nextLine;
        }

        public String toString() {
            return format("%d [%.0f;%.0f]", index, circle.getLatitude(), circle.getLatitude());
        }


        public void markAs(Node selectedRoute) {
            this.circle.circle.setFill(selectedRoute.circle.circle.getFill());
        }

        public void mark() {
            this.circle.circle.setFill(getRandomColor());
        }
        private Color getRandomColor() {
            Random r = new Random();
            return Color.rgb(r.nextInt(255),r.nextInt(255),r.nextInt(255));
        }
    }

    @Data
    @AllArgsConstructor
    private static class MultiInsertInfo {
        List<SingleInsertInfo> singleInsertInfos = new ArrayList<>();
        @Setter
        double delta;
    }


    private static class NodeFactory {

        private final ObservableList<javafx.scene.Node> shapes;
        private final double[][] weights = new double[1000][1000];

        {
            for (int i = 0; i < weights.length; i++) {
                for (int j = 0; j < weights[0].length; j++) {
                    weights[i][j] = Double.MAX_VALUE;
                }

            }
        }

        private final List<Node> allNodes = new ArrayList<>();
        private final List<Node> routes = new ArrayList<>();

        private final Random r = new Random();

        public NodeFactory(ObservableList<javafx.scene.Node> shapes) {
            this.shapes = shapes;

        }

        Node addNode(Position circle) {
            Node node = Node.of(circle);
            updateWeights(node);
            process(node);
            return node;
        }

        public Node addRoute(Position circle) {

            Node node = Node.of(circle);
            node.mark();
            updateWeights(node);
            routes.add(node);
            return node;
        }

        public Node initOnlyNode(Position circle) {
            Node node = Node.of(circle);
            updateWeights(node);
            return node;
        }

        private void addToRoute(Node selectedRoute, Node node) {

            Node oldNext = selectedRoute.next;


            node.markAs(selectedRoute);
            selectedRoute.next = node;
            node.next = oldNext;
            Line line = getLine(selectedRoute, node);

            if (oldNext != null) {
                Line nextLine = getLine(node, oldNext);

                shapes.remove(selectedRoute.nextLine);
                node.setNextLine(nextLine);
                shapes.add(nextLine);
            }

            selectedRoute.setNextLine(line);
            shapes.add(line);


        }

        private void updateWeights(Node node) {
            allNodes.forEach(old -> {
                double weight = getWeight(node, old);
                weight += 3 * r.nextDouble();
                weights[old.index][node.index] = weight;
                weights[node.index][old.index] = weight;
            });
            allNodes.add(node);
        }

        private double getWeight(Node node, Node old) {
            double x = old.circle.getLatitude();
            double y = old.circle.getLongitude();
            double nx = node.circle.getLatitude();
            double ny = node.circle.getLongitude();
            return Math.sqrt((x - nx) * (x - nx) + (y - ny) * (y - ny));
        }

        public void process(Node node) {
            if (routes.isEmpty()) {
                routes.add(node);
                node.mark();
            } else {

                Map<Node, Double> routeToLenMap = routes.stream().collect(Collectors.toMap(t -> t, t -> calcOnRoute(t, (lenNode, prevValue) -> (lenNode.next != null) ? weights[lenNode.index][lenNode.next.index] + prevValue : prevValue)));
                List<SingleInsertInfo> insertSingleInfos = routes.stream().map(route -> getMinimumLengthModification(route, node, routeToLenMap.get(route))).collect(Collectors.toList());

                List<MultiInsertInfo> multiInsertInfos = insertSingleInfos.stream().map(firstInsert -> {
                    Node insertFirst = firstInsert.insert;
                    double newLen = firstInsert.newLen;
                    Double oldLen = routeToLenMap.get(firstInsert.route);
                    double firstDelta = firstInsert.newLen - oldLen;
                    double delta = firstDelta;
                    List<SingleInsertInfo> insertInfos = new ArrayList<>();
                    insertInfos.add(firstInsert);
                    if (node.originalNext != null) {
                        Node baseNode = insertFirst.next == null ? insertFirst : insertFirst.next;
                        SingleInsertInfo seconInfo = getMinimumLengthModification(baseNode, node.originalNext, calcOnRoute(baseNode, (iterateNode, prevValue) -> (iterateNode.next != null) ? weights[iterateNode.index][iterateNode.next.index] + prevValue : prevValue));

                        Double secondDelta = seconInfo.newLen - oldLen;
                        delta += secondDelta;
                        insertInfos.add(seconInfo);
                    }

                    return new MultiInsertInfo(insertInfos, delta);
                }).collect(Collectors.toList());

                Optional<MultiInsertInfo> near = multiInsertInfos.stream().min((o1, o2) -> (int) (o1.delta - o2.delta));
                near.ifPresent(p ->
                        p.singleInsertInfos.forEach(singleInsertInfo -> addToRoute(singleInsertInfo.insert, singleInsertInfo.challenger)));


            }
        }


        private Line getLine(Node selectedRoute, Node node) {
            return new Line(selectedRoute.circle.getLatitude(), selectedRoute.circle.getLongitude(), node.circle.getLatitude(), node.circle.getLongitude());
        }

        private int findMaxIndex(double[] doubles) {
            int maxIndex = 0;
            double maxValue = doubles[0];
            for (int i = 1; i < doubles.length; i++) {
                if (maxValue < doubles[i]) {
                    maxValue = doubles[i];
                    maxIndex = i;
                }
            }
            return maxIndex;
        }


        private SingleInsertInfo getMinimumLengthModification(Node route, Node challenger, double routeLength) {
            Node next = route;
            Node selectedNode = route;
            double minLength = Double.MAX_VALUE;// commonLength + weights[next.index][challenger.index];
            do {

                double nextWeight = (next.next != null) ? weights[next.index][next.next.index] : 0;
                double challengerWeight = weights[next.index][challenger.index];
                double challengeNextWeight = (next.next != null) ? weights[challenger.index][next.next.index] : 0;


                double length = routeLength - nextWeight + challengerWeight + challengeNextWeight;

                if (length < minLength) {
                    selectedNode = next;
                    minLength = length;
                }

                next = next.next;
            } while (next != null);

            return new SingleInsertInfo(route, selectedNode, challenger, minLength);
            //todo return MergeNode
        }



        private Double calcOnRoute(Node route, BiFunction<Node, Double, Double> function) {
            Node next = route;
            double value = 0;
            do {
                value = function.apply(next, value);
                next = next.next;
            } while (next != null);
            return value;
        }


        private Color getRandomColor() {
            return Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));
        }
    }


    @Data(staticConstructor = "of")
    static class Vector2 {
        final double x, y;

        double scalar(Vector2 v) {
            return x * v.x + y * v.y;
        }

        double length() {
            return Math.sqrt(x * x + y * y);
        }
    }

    @AllArgsConstructor
    private static class Position {
        final Circle circle;
        public double getLatitude() {
            return circle.getCenterX();
        }
        public double getLongitude() {
            return circle.getCenterY();
        }


    }
}
