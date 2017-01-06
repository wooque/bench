function randInt(max) {
    return Math.floor(Math.random() * (max + 1));
}

function randStr(length) {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for(var i=0; i < length; i++) {
        text += possible.charAt(randInt(possible.length));
    }
    return text;
}

function getParams(coin) {
    if (coin < 6) {
        return {something: randStr(64)};
    } else if (coin < 8) {
        return {title: randStr(140), thumb: randStr(140), nc: randInt(1000), nv: randInt(5000)};
    } else {
        return {title: randStr(140), thumb: randStr(140), nc: randInt(1000), nv: randInt(5000)};
    }
}