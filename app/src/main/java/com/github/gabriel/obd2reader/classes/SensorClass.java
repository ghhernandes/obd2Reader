package com.github.gabriel.obd2reader.classes;

import com.github.pires.obd.commands.ObdCommand;

public class SensorClass {
    public String id;
    public String name;
    public String description;
    public String value;
    public ObdCommand cmd;

    public SensorClass(String id, String name, String description, String value, ObdCommand cmd) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.value = value;
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return name + "  " + value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ObdCommand getCmd() {return this.cmd;};

    public void setCmd(ObdCommand cmd) {this.cmd = cmd;};
}
