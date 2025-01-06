package com.github.exp;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.github.exp.binance.*;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;


public class Temp {


    public static void main(String[] args) {
        var x = 0;
        for (int i = 0; i < 10; i++) {
            var val = (double) i / 10;
            x++;
            System.out.println();
            System.out.println(val);
            System.out.println(-Math.sin(2* Math.PI * val));
        }
     //   System.exit(0);
        /*var entryInstance = new Instance(102414.20d, 102414.20d, 1.0, 1L, null);
        entryInstance.setPredicted(LabelProducer.Label.SELL);
        var exitInstance = new Instance(98235.0d, 98235.0d, 1.0, 2L, null);
        var tradeEngine = new TradingEngine(0.0);
        tradeEngine.apply(entryInstance);
        var result = tradeEngine.apply(exitInstance);
        System.out.println(result);*/
       /* double[] a = {1.0, 2.0, 3.0};
        System.out.println(Arrays.toString(Arrays.copyOf(a, a.length + 1)));
        System.exit(0);*/
        //var features = Arrays.stream("SFA(QuantIndicator(16,6,4,MFI(30))[90])[0], SFA(QuantIndicator(16,6,4,PFE(5,10,SMA(28,Close)))[94])[0], SFA(QuantIndicator(16,6,4,PFE(5,10,SMA(28,Close)))[104])[0], SFA(QuantIndicator(16,6,4,RSI(25,Close))[100])[0], SFA(QuantIndicator(16,6,4,MFI(12))[84])[0], SFA(QuantIndicator(16,6,4,WLR(9))[109])[0], SFA(QuantIndicator(16,6,4,PFE(10,5,SMA(8,Close)))[81])[0], SFA(QuantIndicator(16,6,4,MFI(7))[81])[0], SFA(QuantIndicator(16,6,4,WLR(7))[117])[0], SFA(QuantIndicator(16,6,4,PFE(5,10,SMA(6,Close)))[83])[0], SFA(QuantIndicator(16,6,4,RSI(6,Close))[115])[0], SFA(QuantIndicator(16,6,4,SMA(6,Close(HeikenAshi)))[97])[0], SFA(QuantIndicator(16,6,4,MFI(4))[92])[0], SFA(QuantIndicator(16,6,4,MFI(4))[131])[0], SFA(QuantIndicator(16,6,4,PFE(10,5,SMA(4,Close)))[130])[0], SFA(QuantIndicator(16,6,4,RSI(4,Close))[86])[0], SFA(QuantIndicator(16,6,4,RSI(4,Close))[7])[0], SFA(QuantIndicator(16,6,4,PFE(10,5,SMA(3,Close)))[5])[0], SFA(QuantIndicator(16,6,4,PFE(10,5,SMA(3,Close)))[133])[0], SFA(QuantIndicator(16,6,4,MFI(2))[132])[0], SFA(QuantIndicator(16,6,4,PFE(5,10,SMA(2,Close)))[32])[0], SFA(QuantIndicator(16,6,4,PFE(5,10,SMA(2,Close)))[90])[0], SFA(QuantIndicator(16,6,4,PFE(5,10,SMA(2,Close)))[117])[0], SFA(QuantIndicator(16,6,4,RSI(9,Close))[53])[0]".split(",\\s")).map(String::trim).toArray(String[]::new);
        /*var dataset1 = Dataset.load(new File("/home/ubuntu/IdeaProjects/finfeat4j/features1.csv"));
        var dataset2 = Dataset.load(new File("/home/ubuntu/IdeaProjects/finfeat4j/features2.csv"));*/
        /*for (var f : features) {
            var data1 = dataset1.select(f);
            var data2 = dataset2.select(f);
            for (int i = 0; i < data1.data().length; i++) {
                equals(data1.data()[i], data2.data()[i]);
                System.out.println(f + " " + i + " is OK");
            }
        }
        System.out.println("Hello, world!");*/
        //System.out.println("Hello, world!");
        //features = Stream.concat(Arrays.stream(features), List.of("price", "trendPrice", "class").stream()).distinct().toArray(String[]::new);
        //var sfaFile = new File("/home/ubuntu/IdeaProjects/finfeat4j/rmodel.ser");
        var client = new BinanceClient(new UMFuturesClientImpl(), null);
        client.saveToFile(new File("/home/ubuntu/IdeaProjects/finfeat4j/finfeat4j-ml/src/test/resources/com/github/finfeat4j/data_btc.csv"), "BTCUSDT", KlineInterval.ONE_DAY, -1).block();
        System.exit(0);
        /*var baseAssets = Set.of("BTC", "ETH");
        var contractTypes = Set.of("PERPETUAL");
        Supplier<IndicatorSet<Bar>> indicatorSetSupplier = IndicatorSupplier::get;
        String[] finalFeatures = features;
        Supplier<Strategy> defaultStrategy = () -> new BaseSmileNBStrategy(indicatorSetSupplier, sfaFile, finalFeatures, 0.7d, 0.0004d);

        var strategyHandler = new StrategyHandler(client, KlineInterval.ONE_DAY, baseAssets, contractTypes, "USDT", defaultStrategy, new HashMap<>(), null);

        strategyHandler.init().block();*/
//        try {
//            Thread.sleep(100000L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public static boolean equals(double[] a, double[] b) {
        double th = Math.pow(10, -1);
        if (a.length != b.length) {
            throw new IllegalStateException("Arrays have different length");
        }
        for (int i = 0; i < a.length; i++) {
            if (!((Double)a[i]).equals(b[i])) {
                assertThat("On index " + i, a[i], closeTo(b[i], th));
            }
        }
        return true;
    }
}