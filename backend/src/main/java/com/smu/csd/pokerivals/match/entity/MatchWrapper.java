package com.smu.csd.pokerivals.match.entity;

import lombok.Getter;

import java.util.*;

/**
 * This class is used to help reconstruct the Match tree
 */
@Getter
public class MatchWrapper {
    private final Match match;

    public MatchWrapper(Match match) {
        this.match = match;
    }

    private MatchWrapper matchAfter;
    private MatchWrapper matchBeforeA;
    private MatchWrapper matchBeforeB;

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        print(buffer, "", "");
        return buffer.toString();
    }

    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(match.toString());
        buffer.append('\n');

        List<MatchWrapper> list = new ArrayList<>();
        if(matchBeforeA != null){
            list.add(matchBeforeA);
        }
        if(matchBeforeB != null){
            list.add(matchBeforeB);
        }

        for (Iterator<MatchWrapper> it = list.iterator(); it.hasNext();) {
            MatchWrapper next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }

    public static MatchWrapper reconstructTree(Collection<Match> matchesSet){
        List<Match> matches = new ArrayList<>(matchesSet);
        matches.sort(Comparator.comparingInt(Match::getDepth).thenComparingInt(Match::getIndex));

        List<MatchWrapper> result = new ArrayList<>();
        // start from level 1
        int noOfNodesInThisLevel = 1;
        // step 1 create the nodes for this level
        List<MatchWrapper> lastLevel = new ArrayList<>();
        while(!matches.isEmpty()){
            // create all nodes on one level
            List<MatchWrapper> currentLevel = new ArrayList<>();
            for (int i = 0; i< noOfNodesInThisLevel; i++){
                MatchWrapper mw = new MatchWrapper(matches.get(0));
                currentLevel.add(mw);
                result.add(mw);
                matches.remove(0);
            }

            if (noOfNodesInThisLevel % 2 == 0) {
                List<MatchWrapper> temp = new ArrayList<>();
                int size = currentLevel.size();
                int idx = 0;
                while (idx < size/2){
                    temp.add(currentLevel.get(idx));
                    temp.add(currentLevel.get(size-1- idx++));
                }
                currentLevel = temp;
                temp= new ArrayList<>();


                for (int i = 0; i< size/2; i+=2 ){
                    // first pair
                    temp.add(currentLevel.get(i));
                    temp.add(currentLevel.get(i+1));

                    // last pair
                    temp.add(currentLevel.get(size-i-2));
                    temp.add(currentLevel.get(size-i-1));
                }
                currentLevel = temp;
            }


            if (noOfNodesInThisLevel >1){
                for (int i = 0; i < noOfNodesInThisLevel; i+=2){
                    lastLevel.get(i/2).matchBeforeA = currentLevel.get(i);
                    currentLevel.get(i).matchAfter = lastLevel.get(i/2);

                    lastLevel.get(i/2).matchBeforeB = currentLevel.get(i+1);
                    currentLevel.get(i+1).matchAfter = lastLevel.get(i/2);
                }
            }
            lastLevel = currentLevel;
            noOfNodesInThisLevel*=2;
        }
        return result.get(0);
    }

    public static List<MatchRoundDTO> generateDisplayableTreeViaBFS(MatchWrapper root){
        List<MatchRoundDTO> result = new ArrayList<>();
        Queue<MatchWrapper> queue = new ArrayDeque<>();
        // 0 is head, size-1 is tail
        queue.add(root);
        int lastRound = root.getMatch().getRound()+1;
        while(!queue.isEmpty()) {
            var currentNode = queue.remove();
            if (lastRound > currentNode.match.getRound()){
                result.add(0,
                        new MatchRoundDTO(
                                "Round "+ (1+currentNode.getMatch().getRound()),
                                new ArrayList<>()
                        ));
                lastRound = currentNode.match.getRound();
            }
            result.get(0).seeds.add(currentNode.match);
            if(currentNode.matchBeforeA != null){
                queue.add(currentNode.matchBeforeA);
            }
            if(currentNode.matchBeforeB != null){
                queue.add(currentNode.matchBeforeB);
            }
        }
        return result;
    }

    public record MatchRoundDTO(String title, List<Match> seeds){}
    public record TeamInMatchDTO(String id, double score, boolean empty){}


}
