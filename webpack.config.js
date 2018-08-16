const path = require('path');
const VueLoaderPlugin = require('vue-loader/lib/plugin');

module.exports = {
    mode: "development",
    entry: './js_src/index.js',
    output: {
        filename: 'app.js',
        path: path.resolve(__dirname, 'src/main/resources/public/js')
    },
    // resolve: {
    //     alias: {
    //         'vue': path.resolve(__dirname, 'node_modules/vue/dist/vue.js'),
    //         'vue-infinite-scroll': path.resolve(__dirname, 'node_modules/vue-infinite-scroll/vue-infinite-scroll.js'),
    //     }
    // },
    module: {
        rules: [
            {
                test: /\.vue$/,
                loader: 'vue-loader'
            },
        ]
    },
    plugins: [
        new VueLoaderPlugin()
    ],
};