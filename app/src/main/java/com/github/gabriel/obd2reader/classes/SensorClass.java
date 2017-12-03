package com.github.gabriel.obd2reader.classes;

import com.github.pires.obd.commands.ObdCommand;

public class SensorClass {
    private String id;
    private String name;
    private String description;
    private String value;
    private ObdCommand cmd;

    public SensorClass(String id, String name, String description, String value, ObdCommand cmd) {
        this.id = id;
        this.setName(name);
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
        if (name.toLowerCase().equals("vehicle speed".toLowerCase()))
            this.name = "Velocidade";
        else if (name.toLowerCase().equals("engine load".toLowerCase()))
            this.name = "Carga do motor";
        else if (name.toLowerCase().equals("engine rpm".toLowerCase()))
            this.name = "RPM";
        else if (name.toLowerCase().equals("engine runtime".toLowerCase()))
            this.name = "Tempo de execução";
        else if (name.toLowerCase().equals("mass air flow".toLowerCase()))
            this.name = "Fluxo de Ar";
        else if (name.toLowerCase().equals("throttle position".toLowerCase()))
            this.name = "Posição Acelerador";
        else if (name.toLowerCase().equals("air intake temperature".toLowerCase()))
            this.name = "Temp. Entrada de Ar";
        else if (name.toLowerCase().equals("ambient air temperature".toLowerCase()))
            this.name = "Temperatura Ambiente";
        else if (name.toLowerCase().equals("engine coolant temperature".toLowerCase()))
            this.name = "Temperatura Refrigerante do Motor";
        else if (name.toLowerCase().equals("control module power supply".toLowerCase()))
            this.name = "Alimentação do Módulo de Controle";
        else if (name.toLowerCase().equals("command equivalence ratio".toLowerCase()))
            this.name = "Razão de Equivalência de Comando";
        else if (name.toLowerCase().equals("distance traveled with MIL on".toLowerCase()))
            this.name = "Distância Luz de Motor acesa";
//        else if (name.toLowerCase().equals("".toLowerCase()))
//            this.name = name;
//        else if (name.toLowerCase().equals("".toLowerCase()))
//            this.name = name;
//        else if (name.toLowerCase().equals("".toLowerCase()))
//            this.name = ;
        else if (name.toLowerCase().equals("fuel type".toLowerCase()))
            this.name = "Tipo de combustível";
//        else if (name.toLowerCase().equals("".toLowerCase()))
//            this.name = "";
        else
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
