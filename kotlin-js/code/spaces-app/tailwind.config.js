const path = require('path');

module.exports = {
    content: [
        path.join(__dirname, './kotlin/**/*.{js,ts,jsx,tsx}')
    ],
    theme: {
        extend: {},
    },
    plugins: [],
};
