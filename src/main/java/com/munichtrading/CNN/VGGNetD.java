package com.munichtrading.CNN;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * Reference: http://arxiv.org/pdf/1409.1556.pdf
 * http://www.robots.ox.ac.uk/~vgg/research/very_deep/
 * https://gist.github.com/ksimonyan/211839e770f7b538e2d8
 *
 * On ImageNet error proven to decrease with depth but plateaued on the 16 weight layer imagenetExample
 * Following is based on 16 layer
 *
 */

public class VGGNetD {
        private int height;
        private int width;
        private int channels = 3;
        private int outputNum = 1000;
        private long seed = 123;
        private int iterations = 370; // 74 epochs - this based on batch of 256

        public VGGNetD(int height, int width, int channels, int outputNum, long seed, int iterations) {
        this.height = height; // Paper sets size to 224 but this can and should vary - limit to min 100 based on depth & convolutions
        this.width = width; // Paper sets size to 224 but this can and should vary - limit to min 100 based on depth & convolutions
        this.channels = channels; // TODO prepare input to subtract mean RGB value from each pixel
        this.outputNum = outputNum;
        this.seed = seed;
        this.iterations = iterations;
        }

        public MultiLayerConfiguration conf() {
        MultiLayerConfiguration.Builder conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .activation("relu")
                .updater(Updater.NESTEROVS)
                        // TODO pretrain with smaller net for first couple CNN layer weights, use Distribution for rest OR http://jmlr.org/proceedings/papers/v9/glorot10a/glorot10a.pdf with Relu
                .weightInit(WeightInit.RELU)
        //                .dist(new NormalDistribution(0.0, 0.01)) // uncomment if using WeightInit.DISTRIBUTION
                .iterations(iterations)
                .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to prevent vanishing or exploding gradients
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(1e-1)
                .learningRateScoreBasedDecayRate(1e-1)
                .regularization(true)
                .l2(5 * 1e-4)
                .momentum(0.9)
                .list(21)
                .layer(0, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn1")
                        .nIn(channels)
                        .nOut(64)
                        .build())
                .layer(1, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn2")
                        .nOut(64)
                        .build())
                .layer(2, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{2, 2})
                        .name("maxpool1")
                        .build())
                .layer(3, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn3")
                        .nOut(128)
                        .build())
                .layer(4, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn4")
                        .nOut(128)
                        .build())
                .layer(5, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{2, 2})
                        .name("maxpool2")
                        .build())
                .layer(6, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn5")
                        .nOut(256)
                        .build())
                .layer(7, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn6")
                        .nOut(256)
                        .build())
                .layer(8, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn7")
                        .nOut(256)
                        .build())
                .layer(9, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{2, 2})
                        .name("maxpool3")
                        .build())
                .layer(10, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn8")
                        .nOut(512)
                        .build())
                .layer(11, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn9")
                        .nOut(512)
                        .build())
                .layer(12, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn10")
                        .nOut(512)
                        .build())
                .layer(13, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{2, 2})
                        .name("maxpool4")
                        .build())
                .layer(14, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn11")
                        .nOut(512)
                        .build())
                .layer(15, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn12")
                        .nOut(512)
                        .build())
                .layer(16, new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .name("cnn13")
                        .nOut(512)
                        .build())
                .layer(17, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{2, 2})
                        .name("maxpool5")
                        .build())
                .layer(18, new DenseLayer.Builder()
                        .name("ffn1")
                        .nOut(4096)
                        .dropOut(0.5)
                        .build())
                .layer(19, new DenseLayer.Builder()
                        .name("ffn2")
                        .nOut(4096)
                        .dropOut(0.5)
                        .build())
                .layer(20, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .name("output")
                        .nOut(outputNum)
                        .activation("softmax")
                        .build())
                .backprop(true)
                .pretrain(false)
                .cnnInputSize(height,width,channels);

            return conf.build();
        }

        public MultiLayerNetwork init(){
                MultiLayerNetwork model = new MultiLayerNetwork(this.conf());
                model.init();
                return model;
        }


}

