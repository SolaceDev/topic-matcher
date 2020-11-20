package com.solace.maas.topicmatcher.igor;

class PathAwareLevel extends Level {

    private final String pathToLevel;

    PathAwareLevel(String pathToLevel, Level base) {
        super(base);
        this.pathToLevel = pathToLevel;
    }

    public String getPathToLevel() {
        return pathToLevel;
    }

}
