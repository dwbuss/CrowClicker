package com.example.clicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class FantasyFishing {

    Map<String, List<FfSpot>> anglers;
    private List<List<Object>> anglerData;
    private List<List<Object>> standingsData;
    private Map<String, Integer> anglerIndex;

    public List<List<Object>> getStandings() {
        return standingsData;
    }

    public void setStandings(List<List<Object>> ffSheet) {
        standingsData = ffSheet;
    }

    public void loadAnglers(List<List<Object>> ffSheet) {
        anglers = new LinkedHashMap<>();
        anglerIndex = new HashMap<>();
        if (ffSheet.isEmpty()) return;
        List<Object> header = ffSheet.get(0);
        AtomicInteger index = new AtomicInteger(0);
        header.forEach(name -> {
            if (!((String) name).equalsIgnoreCase("Selection")) {
                anglerIndex.put(((String) name).trim(), index.get());
                anglers.put(((String) name).trim(), new ArrayList<>());
                index.getAndIncrement();
            }
        });
        for (int i = 1; i <= ffSheet.size() - 1; i++) {
            List<Object> spots = ffSheet.get(i);
            for (int i1 = 1; i1 <= spots.size() - 1; i1++) {
                String spotName = ((String) spots.get(i1)).trim();
                AtomicBoolean alreadyVirgin = new AtomicBoolean(false);
                if (standingsData != null)
                    standingsData.stream().forEach(x -> {
                        AtomicBoolean sameSpot = new AtomicBoolean(false);
                        x.stream().forEach(y -> {
                            if (((String) y).equalsIgnoreCase(spotName))
                                sameSpot.set(true);
                            if (((String) y).toUpperCase().contains("VIRGIN") && sameSpot.get())
                                alreadyVirgin.set(true);
                        });
                    });
                boolean isVirgin = false;
                if (!alreadyVirgin.get()) {
                    isVirgin = spotName.contains("(") ? spotName.substring(spotName.indexOf('(')).toUpperCase().contains("V") : false;
                } else {
                    isVirgin = false;
                }
                boolean isFranchise = spotName.contains("(") ? spotName.substring(spotName.indexOf('(')).toUpperCase().contains("F") : false;
                boolean isCommunity = spotName.contains("(") ? spotName.substring(spotName.indexOf('(')).toUpperCase().contains("C") : false;
                anglers.get(header.get(i1)).add(new FfSpot(spotName, isVirgin, isFranchise, isCommunity));
            }
        }
    }

    public String[] getLocations() {
        List<String> locations = new ArrayList<>();
        locations.add("");
        anglers.entrySet().stream().forEach(ffSpots -> {
            ffSpots.getValue().stream().forEach(spot -> locations.add(spot.name + " : " + ffSpots.getKey()));
        });
        return locations.toArray(new String[locations.size()]);
    }

    public String[] getOwners() {
        List<String> owners = new ArrayList<>();
        owners.add("");
        anglers.keySet().forEach(x -> {
            owners.add(x);
        });
        return owners.toArray(new String[owners.size()]);
    }

    public List<Object> scoreCatch(String angler, String location, String size, String owner, String date, boolean videoCaptured, boolean isNorthern, boolean lifeVest) {
        // date, angler, size , location, owner , ... anglers
        int columnOffset = 5;
        int bonusColumn = columnOffset + anglers.size();
        String[] newRow = new String[bonusColumn + 1];
        for (int i = columnOffset; i < newRow.length; i++)
            newRow[i] = "";
        newRow[0] = date;
        newRow[1] = angler;
        newRow[2] = size;
        newRow[3] = location;
        newRow[4] = owner;
        if (videoCaptured) newRow[bonusColumn] = newRow[bonusColumn] + " Video";
        if (isNorthern) newRow[bonusColumn] = newRow[bonusColumn] + " Northern";
        if (lifeVest) newRow[bonusColumn] = newRow[bonusColumn] + " LifeVest";
        boolean quarterBonus;
        if (size.endsWith(".25") || size.endsWith(".75")) quarterBonus = true;
        else {
            quarterBonus = false;
        }
        Double points = Double.parseDouble(size);
        if (quarterBonus) points = points - .25;
        // load weather on push and reload ff data on pull

        if (anglers.containsKey(angler)) {
            // angler location
            String spotOwner = anglers.entrySet().stream().filter(x -> x.getValue().stream().anyMatch(s -> s.name.equalsIgnoreCase(location))).findFirst().get().getKey();
            if (spotOwner.equalsIgnoreCase(angler) || anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isCommunity) {
                if (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isFranchise) {
                    points = points * 2;
                    if (quarterBonus) points = points + .25;
                    newRow[bonusColumn] = newRow[bonusColumn] + " Franchise";
                }
                boolean isVirgin = (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isVirgin && !isNorthern);
                if (isVirgin) {
                    points = points + 10;
                    newRow[bonusColumn] = newRow[bonusColumn] + " Virgin";
                }
                if (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isCommunity) {
                    newRow[bonusColumn] = newRow[bonusColumn] + " Community";
                }
                if (videoCaptured) points = points + 10;
                if (lifeVest) points = points + 2;
                if (quarterBonus) points = points + .25;
                Double finalPoints = points;
                Arrays.stream(getOwners()).forEach(o -> {
                    if (!o.isEmpty()) {
                        if (o.equalsIgnoreCase(angler) && (Double.parseDouble(size) >= 40 || isVirgin)) {
                            newRow[columnOffset + anglerIndex.get(o)] = finalPoints + "";
                        }
                    }
                });
            } else { // need to split
                Double finalPoints = points / 2;
                Arrays.stream(getOwners()).forEach(o -> {
                    if (!o.isEmpty()) {
                        if (o.equalsIgnoreCase(angler)) {

                            Double newPoints = finalPoints;
                            boolean isVirgin = (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isVirgin && !isNorthern);
                            if (isVirgin) {
                                newPoints = newPoints + 10;
                                newRow[bonusColumn] = newRow[bonusColumn] + " Virgin";
                            }
                            if (videoCaptured) newPoints = newPoints + 10;
                            if (lifeVest) newPoints = newPoints + 2;

                            if (quarterBonus) newPoints = newPoints + .25;
                            if (Double.parseDouble(size) >= 40 || isVirgin)
                                newRow[columnOffset + anglerIndex.get(angler)] = newPoints + "";
                        } else if (o.equalsIgnoreCase(owner) || o.equalsIgnoreCase(spotOwner)) {
                            Double newPoints = finalPoints;
                            if (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isFranchise) {
                                newPoints = newPoints * 2;
                                newRow[bonusColumn] = newRow[bonusColumn] + " Franchise";
                            }
                            boolean isVirgin = (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isVirgin && !isNorthern);
                            if (Double.parseDouble(size) >= 40 || isVirgin)
                                newRow[columnOffset + anglerIndex.get(o)] = newPoints + "";
                        }
                    }
                });
            }
        } else if (anglers.get(owner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().isPresent()) {
            // owner location
            if (anglers.get(owner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isFranchise) {
                points = points * 2;
                newRow[bonusColumn] = newRow[bonusColumn] + " Franchise";
            }
            boolean isVirgin = (anglers.get(owner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isVirgin && !isNorthern);
            if (isVirgin) {
                points = points + 10;
                newRow[bonusColumn] = newRow[bonusColumn] + " Virgin";
            }
            if (lifeVest) points = points + 2;
            if (videoCaptured) points = points + 10;

            if (quarterBonus) points = points + .25;
            Double finalPoints = points;
            Arrays.stream(getOwners()).forEach(o -> {
                if (!o.isEmpty()) {
                    if (o.equalsIgnoreCase(owner) && (Double.parseDouble(size) >= 40 || isVirgin))
                        newRow[columnOffset + anglerIndex.get(o)] = finalPoints + "";
                }
            });
        } else {
            // another location (find owner)
            String spotOwner = anglers.entrySet().stream().filter(x -> x.getValue().stream().filter(s -> s.name.equalsIgnoreCase(location)).findFirst().isPresent()).findFirst().get().getKey();
            if (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isFranchise) {
                points = points * 2;
                if (quarterBonus) points = points + .25;
                newRow[bonusColumn] = newRow[bonusColumn] + " Franchise";
            }
            boolean isVirgin = (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isVirgin && !isNorthern);
            if (isVirgin) {
                points = points + 10;
                newRow[bonusColumn] = newRow[bonusColumn] + " Virgin";
            }
            if (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isCommunity) {
                // Commnuity owner of catch gets all points
                newRow[bonusColumn] = newRow[bonusColumn] + " Community";
                if (lifeVest) points = points + 2;

                if (quarterBonus) points = points + .25;
                if (videoCaptured) points = points + 10;
                Double finalPoints = points;
                Arrays.stream(getOwners()).forEach(o -> {
                    if (!o.isEmpty()) {
                        if (o.equalsIgnoreCase(owner) && (Double.parseDouble(size) >= 40 || isVirgin))
                            newRow[columnOffset + anglerIndex.get(o)] = finalPoints + "";
                    }
                });
            } else {
                // need to split points between owner and spotOwner
                Double finalPoints = points / 2;
                Arrays.stream(getOwners()).forEach(o -> {
                    if (o.equalsIgnoreCase(owner)) {
                        Double newPoints = finalPoints;
                        if (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isFranchise) {
                            newPoints = newPoints * 2;
                            if (quarterBonus) newPoints = newPoints + .25;
                            newRow[bonusColumn] = newRow[bonusColumn] + " Franchise";
                        }
                        boolean virgin = (anglers.get(spotOwner).stream().filter(x -> x.name.equalsIgnoreCase(location)).findFirst().get().isVirgin && !isNorthern);
                        if (virgin) {
                            newPoints = newPoints + 10;
                            newRow[bonusColumn] = newRow[bonusColumn] + " Virgin";
                        }
                        if (lifeVest) newPoints = newPoints + 2;
                        if (videoCaptured) newPoints = newPoints + 10;

                        if (quarterBonus) newPoints = newPoints + .25;
                        if (Double.parseDouble(size) >= 40 || isVirgin)
                            newRow[columnOffset + anglerIndex.get(owner)] = newPoints + "";
                    } else if (o.equalsIgnoreCase(owner) || o.equalsIgnoreCase(spotOwner) && (Double.parseDouble(size) >= 40 || isVirgin))
                        newRow[columnOffset + anglerIndex.get(o)] = finalPoints + "";
                });
            }
        }
        return Arrays.asList(newRow);
    }

    public List<List<Object>> getAnglerData() {
        return anglerData;
    }

    public void setAnglerData(List<List<Object>> values) {
        anglerData = values;
    }

}
