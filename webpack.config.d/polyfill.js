config.resolve.fallback = {
    "url": false,
    "crypto": false,
    "process": false
}

const providePlugin = new webpack.ProvidePlugin({
    Buffer: ['buffer', 'Buffer'],
    process: 'process/browser',
})
config.plugins.push(providePlugin)
