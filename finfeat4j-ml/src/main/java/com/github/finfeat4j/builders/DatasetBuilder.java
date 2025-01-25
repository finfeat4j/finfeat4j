package com.github.finfeat4j.builders;

import com.github.finfeat4j.api.Bar;
import com.github.finfeat4j.api.LabelProducer;
import com.github.finfeat4j.core.DoubleDataset;
import com.github.finfeat4j.core.IndicatorSet;
import com.github.finfeat4j.fs.mrmre.MRMRe;
import com.github.finfeat4j.label.TrendLabel;
import com.github.finfeat4j.ml.sfa.SFATransformers;
import sfa.transformation.SFA;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DatasetBuilder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatasetBuilder.class);

    /**
     * Use SFA discretization or not
     */
    private boolean useSFA = true;
    /**
     * SFA window size
     */
    private int sfaWindowSize = 32;
    /**
     * Create SFA model or load from file
     */
    private boolean createSFAModel = false;
    /**
     * Use MRMRe feature selection or not
     */
    private boolean useMRMRe = true;
    /**
     * MRMRe features chunk size
     */
    private int MRMReFeaturesChunkSize = 2000;
    /**
     * MRMRe select max features from chunk
     */
    private int MRMReSelectMaxFromChunk = 50;
    /**
     * MRMRe MI (mutual information) threshold for chunk
     */
    private String MIThresholdChunk = "0.001";
    /**
     * MRMRe MI (mutual information) threshold for final selection
     */
    private String MIThresholdFinal = "0.001";
    /**
     * MRMRe causality threshold for chunk
     */
    private String CAThresholdChunk = "0.000";
    /**
     * MRMRe causality threshold for final selection
     */
    private String CAThresholdFinal = "0.000";
    /**
     * Filter features with same values
     */
    private boolean filterSameFeatures = true;
    /**
     * SFA model file
     */
    private File sfaModelFile;
    /**
     * Save MRMRe selected features to file
     */
    private File featureFile;
    /**
     * Dataset file
     */
    private File datasetFile;
    /**
     * Indicator set supplier
     */
    private Supplier<IndicatorSet<Bar>> indicatorSetSupplier;
    /**
     * Label producer supplier
     */
    private Supplier<LabelProducer> labelProducerSupplier = () -> new TrendLabel(0.1);
    /**
     * Stream supplier
     */
    private Supplier<Stream<Bar>> streamSupplier;


    public DatasetBuilder filterSameFeatures(boolean filterSameFeatures) {
        this.filterSameFeatures = filterSameFeatures;
        return this;
    }

    public DatasetBuilder chunkSize(int MRMReFeaturesChunkSize) {
        this.MRMReFeaturesChunkSize = MRMReFeaturesChunkSize;
        return this;
    }

    public DatasetBuilder selectMax(int MRMReSelectMaxFromChunk) {
        this.MRMReSelectMaxFromChunk = MRMReSelectMaxFromChunk;
        return this;
    }

    public DatasetBuilder miThreshold(String MIThresholdChunk) {
        this.MIThresholdChunk = MIThresholdChunk;
        return this;
    }

    public DatasetBuilder miFinalThreshold(String MIThresholdFinal) {
        this.MIThresholdFinal = MIThresholdFinal;
        return this;
    }

    public DatasetBuilder caThreshold(String CAThresholdChunk) {
        this.CAThresholdChunk = CAThresholdChunk;
        return this;
    }

    public DatasetBuilder caThresholdFinal(String CAThresholdFinal) {
        this.CAThresholdFinal = CAThresholdFinal;
        return this;
    }

    public DatasetBuilder sfaWindowSize(int sfaWindowSize) {
        this.sfaWindowSize = sfaWindowSize;
        return this;
    }

    public DatasetBuilder datasetFile(File datasetFile) {
        this.datasetFile = datasetFile;
        return this;
    }

    public DatasetBuilder stream(Supplier<Stream<Bar>> streamSupplier) {
        this.streamSupplier = streamSupplier;
        return this;
    }

    public DatasetBuilder useSFA(boolean useSFA) {
        this.useSFA = useSFA;
        return this;
    }

    public DatasetBuilder sfaModelFile(File sfaModelFile) {
        this.sfaModelFile = sfaModelFile;
        return this;
    }

    public DatasetBuilder indicators(Supplier<IndicatorSet<Bar>> indicatorSetSupplier) {
        this.indicatorSetSupplier = indicatorSetSupplier;
        return this;
    }

    public DatasetBuilder createSFAModel(boolean createSFAModel) {
        this.createSFAModel = createSFAModel;
        return this;
    }

    public DatasetBuilder useMRMRe(boolean useMRMRe) {
        this.useMRMRe = useMRMRe;
        return this;
    }

    public DatasetBuilder featureFile(File featureFile) {
        this.featureFile = featureFile;
        return this;
    }

    public DatasetBuilder labelProducer(Supplier<LabelProducer> labelProducerSupplier) {
        this.labelProducerSupplier = labelProducerSupplier;
        return this;
    }

    public DoubleDataset createDataset() {
        if (datasetFile == null) {
            throw new IllegalArgumentException("Dataset file is not set");
        }
        if (streamSupplier == null) {
            throw new IllegalArgumentException("Stream supplier is not set");
        }
        if (indicatorSetSupplier == null) {
            throw new IllegalArgumentException("Indicator set supplier is not set");
        }
        if (labelProducerSupplier == null) {
            throw new IllegalArgumentException("Label producer supplier is not set");
        }
        if (useSFA && sfaModelFile == null) {
            throw new IllegalArgumentException("SFA model file is not set");
        }
        if (useMRMRe && featureFile == null) {
            throw new IllegalArgumentException("Feature file is not set");
        }
        var indicatorSet = indicatorSetSupplier.get();
        var stream = streamSupplier;
        var withLabels = indicatorSet.withLabels(stream.get(), labelProducerSupplier.get(), stream.get().count());
        if (useSFA) {
            SFATransformers transformers;
            if (!createSFAModel) {
                transformers = SFATransformers.load(sfaModelFile);
            } else {
                transformers = SFATransformers.fit(withLabels,
                        SFA.HistogramType.EQUI_FREQUENCY, 4, 2, sfaWindowSize, sfaModelFile, "class",
                        0.5d, "trendPrice", "price", "class"
                );
            }
            withLabels = transformers.asIndicatorSet(withLabels.features())
                    .asDataset(Arrays.stream(withLabels.data()), sfaWindowSize);
        }
        log.info("Dataset created");
        if (filterSameFeatures) {
            withLabels = filterSameFeatures(withLabels);
        }
        withLabels.save(datasetFile);
        log.info("Dataset saved to {}", datasetFile.getAbsolutePath());
        var mrmreSelected = MRMRe.mRMRe(
                datasetFile.getAbsolutePath(),
                MRMReFeaturesChunkSize,
                MRMReSelectMaxFromChunk,
                MIThresholdChunk,
                MIThresholdFinal,
                CAThresholdChunk,
                CAThresholdFinal
        );
        log.info("MRMRe selected features: {}", mrmreSelected.length);
        writeFeatureFile(mrmreSelected);
        return withLabels;
    }

    private void writeFeatureFile(String[] features) {
        try (var writer = new FileWriter(featureFile, false)) {
            writer.write(String.join("/", features));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DoubleDataset filterSameFeatures(DoubleDataset dataset) {
        Set<String> hashes = Collections.newSetFromMap(new ConcurrentHashMap<>());
        List<String> columnsToKeep = Collections.synchronizedList(new ArrayList<>());
        columnsToKeep.add("price");
        columnsToKeep.add("trendPrice");
        columnsToKeep.add("class");
        IntStream.range(0, dataset.features().length).parallel().forEach(i -> {
            var values = dataset.column(i);
            ByteBuffer buffer = ByteBuffer.allocate(values.length * Double.BYTES);
            for (double value : values) {
                buffer.putDouble(value);
            }
            var bytes = buffer.array();
            try {
                var md = MessageDigest.getInstance("SHA-256");
                var hash = md.digest(bytes);
                var hashString = Base64.getEncoder().encodeToString(hash);
                if (hashes.add(hashString)) {
                    columnsToKeep.add(dataset.features()[i]);
                } else {
                    System.out.println("Not unique column: " + dataset.features()[i]);
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
        return (DoubleDataset) dataset.select(
           columnsToKeep.stream().distinct().toArray(String[]::new)
        );
    }
}
