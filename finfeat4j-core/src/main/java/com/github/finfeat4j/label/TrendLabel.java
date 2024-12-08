package com.github.finfeat4j.label;

import java.math.BigDecimal;

/**
 * @Article
 * {e22101162,
 * AUTHOR = {Wu, Dingming and Wang, Xiaolong and Su, Jingyong and Tang, Buzhou and Wu, Shaocong},
 * TITLE = {A Labeling Method for Financial Time Series Prediction Based on Trends},
 * JOURNAL = {Entropy},
 * VOLUME = {22},
 * YEAR = {2020},
 * NUMBER = {10},
 * ARTICLE-NUMBER = {1162},
 * URL = {https://www.mdpi.com/1099-4300/22/10/1162},
 * PubMedID = {33286931},
 * ISSN = {1099-4300},
 * ABSTRACT = {Time series prediction has been widely applied to the finance industry in applications such as stock market price and commodity price forecasting. Machine learning methods have been widely used in financial time series prediction in recent years. How to label financial time series data to determine the prediction accuracy of machine learning models and subsequently determine final investment returns is a hot topic. Existing labeling methods of financial time series mainly label data by comparing the current data with those of a short time period in the future. However, financial time series data are typically non-linear with obvious short-term randomness. Therefore, these labeling methods have not captured the continuous trend features of financial time series data, leading to a difference between their labeling results and real market trends. In this paper, a new labeling method called “continuous trend labeling” is proposed to address the above problem. In the feature preprocessing stage, this paper proposed a new method that can avoid the problem of look-ahead bias in traditional data standardization or normalization processes. Then, a detailed logical explanation was given, the definition of continuous trend labeling was proposed and also an automatic labeling algorithm was given to extract the continuous trend features of financial time series data. Experiments on the Shanghai Composite Index and Shenzhen Component Index and some stocks of China showed that our labeling method is a much better state-of-the-art labeling method in terms of classification accuracy and some other classification evaluation metrics. The results of the paper also proved that deep learning models such as LSTM and GRU are more suitable for dealing with the prediction of financial time series data.},
 * DOI = {10.3390/e22101162}
 * }
 */
public class TrendLabel implements LabelProducer {
    private double FP = Double.NaN;
    private double x_H;
    private long HT;
    private double x_L;
    private long LT;
    private int Cid;
    private final double threshold;
    private long id;

    /**
     * @param threshold Change threshold in percentage 0.05 = 5%
     */
    public TrendLabel(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public Result[] apply(BigDecimal in) {
        Result state = null;
        id++;
        var x = in.doubleValue();
        if (Double.isNaN(this.FP)) {
            this.FP = x;
            this.x_H = x;
            this.x_L = x;
        }
        if (Cid == 0) {
            if (x > FP + FP * threshold) {
                x_H = x;
                HT = id;
                Cid = 1;
            } else if (x < FP - FP * threshold) {
                x_L = x;
                LT = id;
                Cid = -1;
            }
        } else if (Cid > 0) {
            if (x > x_H) {
                x_H = x;
                HT = id;
            } else if (x < x_H - x_H * threshold && LT < HT) {
                // from LT to HT is uptrend return reversal
                state = new Result(HT, BigDecimal.valueOf(x_H), Label.SELL);
                x_L = x;
                LT = id;
                Cid = -1;
            }
        } else {
            if (x < x_L) {
                x_L = x;
                LT = id;
            } else if (x > x_L + x_L * threshold && HT < LT) {
                // from HT to LT is downtrend return reversal
                state = new Result(LT, BigDecimal.valueOf(x_L), Label.BUY);
                x_H = x;
                HT = id;
                Cid = 1;
            }
        }
        return state == null ? new Result[0] : new Result[] {state};
    }
}
