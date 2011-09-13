Object.defineProperty(Array.prototype, "say", {value: function() {
	    for(var i = 0; i < this.length; i++) {
	        bot.currentChannel.say(this[i]);
	    }
	},
    enumerable: false});

String.prototype.paste = function(language) {
    return util.paste(this, language);
}

String.prototype.controlv = function() {
    return util.controlv(this);
}

String.prototype.multiline = function() {
    this.split("\n").say();
}

String.prototype.c = function() {
    var channel = this.substring(0,1) == "#" ? this : "#" + this;    
    return bot.getChannel(channel);
}

String.prototype.say = function(message) {
    this.c().say(message);
}

String.prototype.action = function(action) {
	this.c().action(action);
}

String.prototype.kick = function(reason) {
	bot.currentChannel.users[this].kick(reason);
}

String.prototype.ban = function(reason) {
	bot.currentChannel.users[this].ban(reason);
}

String.prototype.reverse = function() {
	return this.split("").reverse().join("");
}

String.prototype.trim = function() {
	return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}

Channel.prototype.action = function(action) {
    this.say("\1ACTION " + action + "\1");
}

ChannelUser.prototype.ban = function(reason) {
    if(typeof(reason) == "undefined") {
        reason = "Pwnt by Mojo";
    }
    this.channel.mode("+b *!*@" + this.host);
    this.kick(reason);
}


Channel.prototype.$ = Bot.prototype.$ = function (u) {
    if (/\s+/.test(u)) {
        var o = this, a = u.split(/\s+/g);
        for (var i in a) {
            o = o.$(a[i]);
        }
        return o;
    }
    if (u.substring(0, 1) == ":") {
        return bots[u.substring(1)];
    } else {
        if (u.substring(0, 1) == "#") {
            var c = (this instanceof Bot) ? this.getChannel(u) : this.bot.getChannel(u);
            c.__call__ = function (t) {
                c.say(t);
            };
            return c;
        } else {
            return this.users[u];
        }
    }
}

Channel.prototype.$.__noSuchMethod__ = function (n, a) {
    return users[n];
}


Channel.prototype.setContextual = function(contextual, value) {
	var db = new JSDB("contextual_" + this.bot.identifier);
	db.set(contextual + "_" + this.name, Boolean(value));
}

Channel.prototype.getContextual = function(contextual) {
	var db = new JSDB("contextual_" + this.bot.identifier);
	return db.get(contextual + "_" + this.name) == "true";
}

Channel.prototype.setSearch = function(search, value) {
	var db = new JSDB("search_" + this.bot.identifier);
	db.set(search + "_" + this.name, Boolean(value));
}

Channel.prototype.getSearch = function(search) {
	var db = new JSDB("search_" + this.bot.identifier);
	return db.get(search + "_" + this.name) == "true";
}


Channel.prototype.sc = function() {
	this.setSearch("tinysong", true);
	this.setSearch("spotify", true);
	this.setSearch("youtube", true);
	this.setContextual("youtube", true);
	this.setContextual("vimeo", true);
	this.setContextual("metacafe", true);
	this.setContextual("spotify", true);
}

Channel.prototype.scOff = function() {
	this.setSearch("tinysong", false);
	this.setSearch("spotify", false);
	this.setSearch("youtube", false);
	this.setContextual("youtube", false);
	this.setContextual("vimeo", false);
	this.setContextual("metacafe", false);
	this.setContextual("spotify", false);
}

Channel.prototype.contextuals = {};

for each(c in ["youtube", "vimeo", "spotify", "metacafe"]) {
	Channel.prototype.contextuals.__defineGetter__(c, function() {return this.getContextual(c)});
	Channel.prototype.contextuals.__defineSetter__(c, function(value) {return this.setContextual(value)})	
}


Channel.prototype.searches = {}


for each(c in ["spotify", "tinyurl"]) {
	Channel.prototype.searches.__defineGetter__(c, function() {return this.getSearch(c)});
	Channel.prototype.searches.__defineSetter__(c, function(value) {return this.setSearch(value)})	
}

delete c;

white = function(str) {
	return "\x0300" + str + "\x0300";
}

white.toString = function() {
	return "\x0300";
}

String.prototype.white = function() {
	return white(this);
}

black = function(str) {
	return "\x0301" + str + "\x0301";
}

black.toString = function() {
	return "\x0300";
}

String.prototype.black = function() {
	return black(this);
}

darkblue = function(str) {
	return "\x0302" + str + "\x0302";
}

darkblue.toString = function() {
	return "\x0302";
}

String.prototype.darkblue = function() {
	return darkblue(this);
}

green = function(str) {
	return "\x0303" + str + "\x0303";
}

green.toString = function() {
	return "\x0303";
}

String.prototype.green = function() {
	return green(this);
}

red = function(str) {
	return "\x0304" + str + "\x0304";
}

red.toString = function() {
	return "\x0304";
}

String.prototype.red = function() {
	return red(this);
}

maroon = function(str) {
	return "\x0305" + str + "\x0305";
}

maroon.toString = function() {
	return "\x0305";
}

String.prototype.maroon = function() {
	return maroon(this);
}

purple = function(str) {
	return "\x0306" + str + "\x0306";
}

purple.toString = function() {
	return "\x0306";
}

String.prototype.purple = function() {
	return purple(this);
}

orange = function(str) {
	return "\x0307" + str + "\x0307";
}

orange.toString = function() {
	return "\x0307";
}

String.prototype.orange = function() {
	return orange(this);
}

yellow = function(str) {
	return "\x0308" + str + "\x0308";
}

yellow.toString = function() {
	return "\x0308";
}

String.prototype.yellow = function() {
	return yellow(this);
}

lightgreen = function(str) {
	return "\x0309" + str + "\x0309";
}

lightgreen.toString = function() {
	return "\x0309";
}

String.prototype.lightgreen = function() {
	return lightgreen(this);
}

teal = function(str) {
	return "\x0310" + str + "\x0310";
}

teal.toString = function() {
	return "\x0310";
}

String.prototype.teal = function() {
	return teal(this);
}

lightblue = function(str) {
	return "\x0311" + str + "\x0311";
}

lightblue.toString = function() {
	return "\x0311";
}

String.prototype.lightblue = function() {
	return lightblue(this);
}

blue = function(str) {
	return "\x0312" + str + "\x0312";
}

blue.toString = function() {
	return "\x0312";
}

String.prototype.blue = function() {
	return blue(this);
}

magenta = function(str) {
	return "\x0313" + str + "\x0313";
}

magenta.toString = function() {
	return "\x0313";
}

String.prototype.magenta = function() {
	return magenta(this);
}

darkgray = function(str) {
	return "\x0314" + str + "\x0314";
}

darkgray.toString = function() {
	return "\x0314";
}

String.prototype.darkgray = function() {
	return darkgray(this);
}

lightgray = function(str) {
	return "\x0315" + str + "\x0315";
}

lightgray.toString = function() {
	return "\x0315";
}

String.prototype.lightgray = function() {
	return lightgray(this);
}

underline = function(str) {
	return "\x1f" + str + "\x1f";
}

underline.toString = function() {
	return "\x1f";
}

String.prototype.underline = function() {
	return underline(this);
}

bold = function(str) {
	return "\x02" + str + "\x02";
}

bold.toString = function() {
	return "\x02";
}

String.prototype.bold = function() {
	return bold(this);
}

normal = function(str) {
	return "\x0f" + str + "\x0f";
}

normal.toString = function() {
	return "\x0f";
}

String.prototype.normal = function() {
	return normal(this);
}


String.prototype.no = function() {
	return this.replace(/&gt;/g, ">").replace(/&lt;/g, "<").replace(/&quot;/g, '"').replace(/&apos;/g, "'").replace(/&amp;/g, "&");
}

youtube = {};
youtube.get = function(id) {
	return JSON.parse(getUrl("http://gdata.youtube.com/feeds/api/videos/" + encodeURIComponent(id) + "?v=2&alt=jsonc"));
}

youtube.search = function(q, orderby) {
	return JSON.parse(getUrl("http://gdata.youtube.com/feeds/api/videos?q=" + encodeURIComponent(q) + "&v=2&alt=jsonc&orderby=" + (orderby || "relevance")));
}

youtube.formatVideo = function(video) {
	var str = "" +  orange + "[YouTube video]" + green + " \"" + video.title.no() + "\" " +
	normal + "by " + blue + video.uploader.no() + normal;

	if(typeof(video.viewCount) == "number" || typeof(video.favoriteCount) == "number") {
		var favorite = typeof(video.favoriteCount) == "number" ? video.favoriteCount : 0;
		var view = typeof(video.viewCount) == "number" ? video.viewCount : 0;
		str += " favorited/viewed " + magenta + String(favorite).reverse().replace(/(\d\d\d)/g, "$1 ").reverse().trim() +
		" / " + String(view).reverse().replace(/(\d\d\d)/g, "$1 ").reverse().trim() + normal;
	}
	if(typeof(video.duration) == "number") {
		var seconds = video.duration % 60;
		str += " (" + parseInt(video.duration / 60) + ":" + (seconds < 10 ? "0" + seconds : seconds) + ")";
	} else {
		str += " (unknown duration)";
	}
	return str;
}

youtube.formatURL = function(id) {
	return "http://youtu.be/" + encodeURIComponent(id) ;
}

YTVideo = function(id) {
	this._id = id;
	this._video = youtube.get(id);
	this.__proto__ = this._video.data;
	this._video.data.__proto__ = YTVideo.prototype;
}

Object.defineProperty(YTVideo.prototype,
	"shortURL",
	{get:
		function() {
			return youtube.formatURL(this.id);
		}
	}
);

Object.defineProperty(YTVideo.prototype,
	"format",
	{value:
		function() {
			return youtube.formatVideo(this._video.data);
		}
	}
);
