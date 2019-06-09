package org.repocrud.service.route;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class RoutesUtils {
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

        static Node of(Circle circle) {
            return new Node(circle);
        }

        public void setNextLine(Line nextLine) {
            this.nextLine = nextLine;
        }

        public String toString() {
            return format("%d [%.0f;%.0f]", index, circle.getCenterX(), circle.getCenterY());
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

        public NodeFactory(ObservableList<javafx.scene.Node> shapes, RouteOptimizator routeOptimizator) {
            this.shapes = shapes;
            this.routeOptimizator = routeOptimizator;

        }

        Node addNode(Circle circle) {
            Node node = Node.of(circle);
            updateWeights(node);
            process(node);
            return node;
        }

        public Node addRoute(Circle circle) {
            circle.setFill(getRandomColor());
            Node node = Node.of(circle);
            updateWeights(node);
            routes.add(node);
            return node;
        }

        public Node initOnlyNode(Circle circle) {
            Node node = Node.of(circle);
            updateWeights(node);
            return node;
        }

        private void addToRoute(Node selectedRoute, Node node) {

            Node oldNext = selectedRoute.next;

            node.circle.setFill(selectedRoute.circle.getFill());
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
            double x = old.circle.getCenterX();
            double y = old.circle.getCenterY();
            double nx = node.circle.getCenterX();
            double ny = node.circle.getCenterY();
            return Math.sqrt((x - nx) * (x - nx) + (y - ny) * (y - ny));
        }

        public void process(Node node) {
            if (routes.isEmpty()) {
                routes.add(node);
                node.circle.setFill(getRandomColor());
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
            return new Line(selectedRoute.circle.getCenterX(), selectedRoute.circle.getCenterY(), node.circle.getCenterX(), node.circle.getCenterY());
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


        static class MergeNode {
            /*
            Pairs of challenge node and node of main route
             */
            List<Pair<Node, Node>> insertAfter = new ArrayList<>();
            double length;
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
}
