import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Scanner;
import java.util.ArrayList;

public class hello {

    public static void main(String[] args){

        Scanner in = new Scanner(System.in);
        ArrayList<Relation> relations = new ArrayList<Relation>();
        ArrayList<Existence> existences = new ArrayList<Existence>();

        generate(existences);

        System.out.print("[L]aki-laki / [P]erempuan? ");
        String s = in.next();
        System.out.println("");

        String couple = "Istri";
        if(s.equals("P")){
            couple = "Suami";
        }

        String[] listRelations = new String[]{couple, "Ayah", "Ibu", "Putra", "Putri"};

        int nb;
        for(int i = 0; i < listRelations.length; i++){
            // di sini cek blocker
            String rel = listRelations[i];

            boolean number = false;
            try {
                number = existences.stream()
                        .filter(existence -> existence.status.equals(rel)).findFirst().get().isNumber;
            } catch(Exception ex){}

            String question = "[Y]/[N]? ";
            if(number) question = "(jumlah)? ";
            System.out.print(rel + question);
            s = in.next();

            nb = 0;
            if(s.equals("Y")) nb = 1;
            else if(s.equals("N")) nb = 0;
            else nb = Integer.parseInt(s);

            relations.add(new Relation(rel, nb));
        }

        /*System.out.println(relations);
        System.out.println(existences);
*/
        // hitung yang diperoleh oleh setiap anggota keluarga
        ArrayList<Portion> listPortions = new ArrayList<Portion>();
        for(int i = 0; i < relations.size(); i++){
            Relation r = relations.get(i);
            if(r.numbers == 0) continue;

            Rule[] rules = existences.stream()
                    .filter(existence -> existence.status.equals(r.status)).findFirst().get().rules;

            boolean ruleFound = false;
            int appliedRule = -1;

            for(int j = 0; j < rules.length && !ruleFound; j++) {
                if (rules[j].premises.size() == 0) {
                    ruleFound = true;
                    appliedRule = j;
                } else {
                    ArrayList<Premise> testedPremises = rules[j].premises;
                    boolean stillValid = true;
                    for (int k = 0; k < testedPremises.size() && stillValid; k++) {
                        Premise testedPremise = testedPremises.get(k);

                        // di sini harus cek relation vs tested premise
                        //System.out.println(testedPremise.status);

                        int nbRelationTested = 0;

                        try {
                            nbRelationTested = relations.stream().
                                    filter(relation -> relation.status.equals(testedPremise.status)).findFirst().get().numbers;
                        } catch (Exception ex) {
                        }

                        if (testedPremise.minMax == Premise.Minmax.MAX && testedPremise.number < nbRelationTested)
                            stillValid = false;
                        else if (testedPremise.minMax == Premise.Minmax.MIN && testedPremise.number > nbRelationTested)
                            stillValid = false;
                    }
                    if (stillValid) {
                        ruleFound = true;
                        appliedRule = j;
                    }
                }
            }

            if(!ruleFound) System.out.println("whaaaaat");
            else{
                listPortions.add(new Portion(r.status, r.numbers, rules[appliedRule].portion, rules[appliedRule].base));
                //System.out.println("rule yang dipakai utk " + r.status + " adalah rule ke-" + appliedRule);
            }
        }

        Collections.sort(listPortions);

        double totalWhole = listPortions.stream().
                filter(portion -> portion.base == Rule.Base.WHOLE).
                mapToDouble(portion -> portion.portion).sum();

        double totalRemainder = listPortions.stream().
                filter(portion -> portion.base == Rule.Base.REMAINDER).
                mapToDouble(portion -> portion.portion).sum();

        double totalComparison =  listPortions.stream().
                filter(portion -> portion.base == Rule.Base.COMPARISON).
                mapToDouble(portion -> portion.portion * portion.nb).sum();

        //System.out.println(totalWhole + " ----- " + totalRemainder + " ----- " + totalComparison);

        double total = 1200000;
        System.out.println("Asumsi total warisan Rp1,200,000");

        DecimalFormat df = new DecimalFormat("Rp#,###.##");

        //TODO: nambahin sistem aul
        if(totalWhole > 1.0) System.out.println("Harus pake aul, belum selesai diprogram");
        else if(totalWhole < 1.0 && totalRemainder != 1.0 && totalComparison == 0.0) System.out.println("bug, need to be checked");
        else{
            double totalUsage = 0;
            for(int i = 0; i < listPortions.size(); i++){
                Portion portion = listPortions.get(i);
                double usage = 0;
                if(portion.base == Rule.Base.WHOLE){
                    usage = portion.portion * total / portion.nb;
                    System.out.println("WHOLE -- " + portion.status + " mendapatkan " + df.format(usage));
                } else if(portion.base == Rule.Base.REMAINDER){
                    usage = portion.portion * total * (1-totalWhole) / portion.nb;
                    System.out.println("REMAINDER -- " + portion.status + " mendapatkan " + df.format(usage));
                } else {
                    usage = portion.portion * total * (1 - totalWhole) / totalComparison;
                    System.out.println("COMPARISON -- " + portion.status + " mendapatkan " + df.format(usage));
                }

                totalUsage += usage * portion.nb;
                System.out.println("Total warisan yang sudah diberikan adalah " + df.format(totalUsage));
                System.out.println("");
            }
        }
    }

    public static void generate(ArrayList<Existence> existences){
        // RULE SUAMI

        Premise premiseNoSon = new Premise("Putra", Premise.Minmax.MAX, 0);
        Premise premiseHasSon = new Premise("Putra", Premise.Minmax.MIN, 1);
        Premise premiseNoDaughter = new Premise("Putri", Premise.Minmax.MAX, 0);
        Premise premiseHasDaughter = new Premise("Putri", Premise.Minmax.MIN, 1);
        Premise premiseHasFather = new Premise("Ayah", Premise.Minmax.MIN, 1);
        Premise premiseHasMother = new Premise("Ibu", Premise.Minmax.MIN, 1);
        Premise premiseHasHusband = new Premise("Suami", Premise.Minmax.MIN, 1);
        Premise premiseHasWife = new Premise("Istri", Premise.Minmax.MIN, 1);
        Premise premiseHasOneDaughter = new Premise("Putri", Premise.Minmax.MAX, 1);

        ArrayList<Premise> premisesSuami1 = new ArrayList<Premise>();
        premisesSuami1.add(premiseNoSon);
        premisesSuami1.add(premiseNoDaughter);

        Rule ruleSuami1 = new Rule(premisesSuami1, 0.5, Rule.Base.WHOLE);
        Rule ruleSuami2 = new Rule(new ArrayList<Premise>(), 0.25, Rule.Base.WHOLE);
        Rule[] ruleSuamis = new Rule[]{ruleSuami1, ruleSuami2};

        existences.add(new Existence("Suami", false, null, ruleSuamis));

        // RULE ISTRI

        ArrayList<Premise> premisesIstri1 = new ArrayList<Premise>();
        premisesIstri1.add(premiseNoSon);
        premisesIstri1.add(premiseNoDaughter);

        Rule ruleIstri1 = new Rule(premisesIstri1, 0.25, Rule.Base.WHOLE);
        Rule ruleIstri2 = new Rule(new ArrayList<Premise>(), 0.125, Rule.Base.WHOLE);
        Rule[] ruleIstris = new Rule[]{ruleIstri1, ruleIstri2};

        existences.add(new Existence("Istri", false, null, ruleIstris));

        // RULE AYAH
        ArrayList<Premise> premisesAyah1 = new ArrayList<Premise>();
        premisesAyah1.add(premiseHasSon);

        Rule ruleAyah1 = new Rule(premisesAyah1, 1.0/6.0, Rule.Base.WHOLE);

        ArrayList<Premise> premisesAyah2 = new ArrayList<Premise>();
        premisesAyah2.add(premiseNoSon);
        premisesAyah2.add(premiseNoDaughter);
        premisesAyah2.add(premiseHasFather);
        premisesAyah2.add(premiseHasMother);
        premisesAyah2.add(premiseHasHusband);

        Rule ruleAyah2 = new Rule(premisesAyah2, 2.0/3.0, Rule.Base.REMAINDER);

        ArrayList<Premise> premisesAyah3 = new ArrayList<Premise>();
        premisesAyah3.add(premiseNoSon);
        premisesAyah3.add(premiseNoDaughter);
        premisesAyah3.add(premiseHasFather);
        premisesAyah3.add(premiseHasMother);
        premisesAyah3.add(premiseHasWife);

        Rule ruleAyah3 = new Rule(premisesAyah3, 2.0/3.0, Rule.Base.REMAINDER);

        ArrayList<Premise> premisesAyah4 = new ArrayList<Premise>();
        premisesAyah4.add(premiseNoSon);
        premisesAyah4.add(premiseHasDaughter);

        //TODO: ini harusnya 1/6 + sisa jika memiliki ashabul furudh
        Rule ruleAyah4 = new Rule(premisesAyah4, 1.0, Rule.Base.REMAINDER);

        //TODO: RULE yang belum kelar kalo hanya dia sendiri

        Rule[] ruleAyahs = new Rule[]{ruleAyah1, ruleAyah2, ruleAyah3, ruleAyah4};

        existences.add(new Existence("Ayah", false, null, ruleAyahs));

        //TODO: ini harusnya tidak memiliki putra/i dan kebawahnya

        // RULE IBU

        ArrayList<Premise> premisesIbu1 = new ArrayList<Premise>();

        premisesIbu1.add(premiseNoSon);
        premisesIbu1.add(premiseNoDaughter);
        premisesIbu1.add(premiseHasFather);
        premisesIbu1.add(premiseHasMother);
        premisesIbu1.add(premiseHasHusband);

        Rule ruleIbu1 = new Rule(premisesIbu1, 1.0/3.0, Rule.Base.REMAINDER);

        ArrayList<Premise> premisesIbu2 = new ArrayList<Premise>();
        premisesIbu2.add(premiseNoSon);
        premisesIbu2.add(premiseNoDaughter);
        premisesIbu2.add(premiseHasFather);
        premisesIbu2.add(premiseHasMother);
        premisesIbu2.add(premiseHasWife);

        Rule ruleIbu2 = new Rule(premisesIbu2, 1.0/3.0, Rule.Base.REMAINDER);

        ArrayList<Premise> premisesIbu3 = new ArrayList<Premise>();
        premisesIbu3.add(premiseNoSon);
        premisesIbu3.add(premiseNoDaughter);

        Rule ruleIbu3 = new Rule(premisesIbu3, 1.0/3.0, Rule.Base.WHOLE);

        Rule ruleIbu4 = new Rule(new ArrayList<Premise>() , 1.0/6.0, Rule.Base.WHOLE);

        Rule[] ruleIbus = new Rule[]{ruleIbu1, ruleIbu2, ruleIbu3, ruleIbu4};
        existences.add(new Existence("Ibu", false, null, ruleIbus));

        // RULE PUTRA

        ArrayList<Premise> premisesPutra1 = new ArrayList<Premise>();

        premisesPutra1.add(premiseHasDaughter);

        Rule rulePutra1 = new Rule(premisesPutra1, 2.0, Rule.Base.COMPARISON);

        Rule rulePutra2 = new Rule(new ArrayList<Premise>(), 1.0, Rule.Base.REMAINDER);

        Rule[] rulePutras = new Rule[]{rulePutra1, rulePutra2};

        existences.add(new Existence("Putra", true, null, rulePutras));

        // RULE PUTRI

        ArrayList<Premise> premisesPutri1 = new ArrayList<Premise>();
        premisesPutri1.add(premiseHasDaughter);
        premisesPutri1.add(premiseHasOneDaughter);
        premisesPutri1.add(premiseNoSon);

        Rule rulePutri1 = new Rule(premisesPutri1, 0.5, Rule.Base.WHOLE);

        ArrayList<Premise> premisesPutri2 = new ArrayList<Premise>();
        premisesPutri2.add(premiseNoSon);

        Rule rulePutri2 = new Rule(premisesPutri2, 2.0/3.0, Rule.Base.WHOLE);

        Rule rulePutri3 = new Rule(new ArrayList<Premise>(), 1.0, Rule.Base.COMPARISON);

        Rule[] rulePutris = new Rule[]{rulePutri1, rulePutri2, rulePutri3};

        existences.add(new Existence("Putri", true, null, rulePutris));
    }
}

class Relation{
    public String status;
    public int numbers;

    public Relation(String newStatus, int newNumbers){
        status = newStatus;
        numbers = newNumbers;
    }

    public boolean isAlive(){
        return numbers > 0;
    }

    @Override
    public String toString(){
        if(numbers == 0) return "tidak memiliki " + status;
        else return "memiliki " + numbers + " orang " + status;
    }
}

class Existence {
    public String status;
    public boolean isNumber;
    public String[] blockers;
    public Rule[] rules;

    public Existence(String newStatus, boolean newIsNumber, String[] newBlockers, Rule[] newRules){
        status = newStatus;
        isNumber = newIsNumber;
        blockers = newBlockers;
        rules = newRules;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("\nGeneral Rules for " + status + ":");
        if(isNumber)
            sb.append("\n  bisa memiliki 0 sampai infinite numbers.");
        else sb.append("\n  either memiliki atau tidak memiliki.");
        //sb.append();

        if(blockers == null)
            sb.append("\n  tidak memiliki penghambat.");
        else {
            sb.append("\n  memiliki penghambat dengan rincian sebagai berikut");
        }


        if(rules.length == 0){
            sb.append("\n  shouldve'nt here");
        } else{
            sb.append("\n  aturan-aturan yang mengikat:");
            for(int i = 0; i < rules.length; i++){
                Rule r = rules[i];
                sb.append("\n  " + i + ".\n");

                sb.append(r.toString());
            }
        }

    return sb.toString();
    }
}

class Rule{
    public ArrayList<Premise> premises;
    public double portion;
    public Base base;
    private static final DecimalFormat df = new DecimalFormat("#.##");

    enum Base {WHOLE, REMAINDER, COMPARISON};

    public Rule(ArrayList<Premise> newMiniRules, double newPortion, Base newBase){
        premises = newMiniRules;
        portion = newPortion;
        base = newBase;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(premises.size() > 0) {
            for (int j = 0; j < premises.size(); j++) {
                Premise mr = premises.get(j);

                String border;
                if(mr.minMax == Premise.Minmax.MIN) {
                    border = "setidaknya";
                } else border = "sebanyaknya";

                if(j > 0) sb.append("\ndan ");
                sb.append("jika " + mr.status + " " + border + " " + mr.number + " orang");

            }
        } else{
            sb.append("jika tidak memenuhi aturan di atas");
        }

        sb.append("\n     maka mendapatkan " + df.format(portion));
        if(base == Rule.Base.WHOLE) sb.append(" dari keseluruhan");
        else if(base == Rule.Base.REMAINDER) sb.append(" dari sisa");
        else sb.append(" dibanding yang memperoleh sisa");

        return sb.toString();
    }
}

class Premise {
    public String status;
    public Minmax minMax; // to define whether this rule applies if something is minimum this number of maximum this number
    public int number;

    enum Minmax {MIN, MAX};

    public Premise(String newStatus, Minmax newMinMax, int newNumber){
        status = newStatus;
        minMax = newMinMax;
        number = newNumber;
    }
}

class Portion implements Comparable<Portion>{
    public String status;
    public int nb;
    public double portion;
    public Rule.Base base;

    public Portion(String newStatus, int newNb, double newPortion, Rule.Base newBase){
        status = newStatus;
        nb = newNb;
        portion = newPortion;
        base = newBase;
    }

    @Override
    public int compareTo(Portion oPortion){
        if(base == Rule.Base.WHOLE) return -1;
        else if(oPortion.base == Rule.Base.WHOLE) return 1;
        else if(base == Rule.Base.REMAINDER) return -1;
        else if(base == Rule.Base.REMAINDER) return 1;
        else return -1;
    }
}
