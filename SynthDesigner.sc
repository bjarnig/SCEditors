SynthDesignerUI {

	*new {|source, sourceArgs, sourceVars, patterns, server, outputDir, colorPrimary, colorSecondary|
		^super.new.display(source, sourceArgs, sourceVars, patterns, server, outputDir, colorPrimary, colorSecondary)
	}

	*help {
	"".postln;
	"// 1-2 Source & Source Args".postln;
	"~designSynth, [\buf, b],".postln;
	"// 3 Source Vars".postln;
	"[".postln;
	"[\pos, ControlSpec(0.0, 1.0, \lin, 0.001, 0.0)],".postln;
	"[\atk, ControlSpec(0.001, 2.0, \lin, 0.001, 0.05)],".postln;
	"[\amp, ControlSpec(0.0, 1.0, \lin, 0.001, 0.5)],".postln;
	"],".postln;
	"// 4 Patterns".postln;
	"[".postln;
		"(name: \"First\", args:[\dur, 0.5]),".postln;
		"(name: \"Third\", args:[\dur, Pbrown(0.05, 0.2, 0.001)]),".postln;
	"],".postln;
	"// 5 server ".postln;
	"s,".postln;
	"// 6 record path".postln;
	"~path ++ \"/output\"".postln;

	}

	display {|source, sourceArgs, sourceVars, patterns, server, outputDir, colorPrimary, colorSecondary|

	var win, play, record, randomize, print, choosePattern, playPattern, titleLabel, isPlaying=false;
    var sourceSynth, sourceDict, sliders, randButtons, patternButtons;
    var addControls, getSliderArgs, playAction, activePattern;
	var lowLine = 70 + ( 50 * sourceVars.size ), y = 65;
	var textColor, knobColor, primaryColor, secondaryColor, font="Monaco";

	// Default Colors

	textColor = Color.white;
	knobColor = Color.fromHexString("#1E2749");
	secondaryColor = Color.fromHexString("#273469");
	primaryColor = Color.fromHexString("#1E2749");

	if(colorPrimary != nil, { primaryColor = colorPrimary});
	if(colorSecondary != nil, { secondaryColor = colorSecondary});

    // Initialize the window & dictionaries

	win = Window.new(source, Rect(200, 100, 600, 160 + (50 * sourceVars.size)));
    win.background = primaryColor;
    win.alpha = 0.95;
    win.front;

	sourceDict = Dictionary();
	sliders = Dictionary();
	randButtons = Dictionary();
	patternButtons = Dictionary();

	titleLabel = StaticText(win, Rect(22,5,150,50));
	titleLabel.font = Font(font, 10);
	titleLabel.string = "SynthDef - Designer";
	titleLabel.stringColor = textColor;

	win.drawFunc = {
		Pen.strokeColor = textColor;
		Pen.moveTo(20@45);
		Pen.lineTo(580@45);
		Pen.stroke;
		Pen.strokeColor = textColor;
		Pen.moveTo(20@lowLine);
		Pen.lineTo(580@lowLine);
		Pen.stroke;
	};


    // SynthDef controls & interface components

    addControls = {|vars, isSource|

		vars.do {|sv|
		var randButton;
		var patternButton;
        var slider;
		var label;
		var numberBox;

		label = StaticText(win, Rect(35,y,100,32));
		label.font =Font(font, 14);
		label.string = sv[0];
		label.stringColor = textColor;

		slider = Slider(win, Rect(125, y, 190, 32));
		slider.background = textColor;
		slider.knobColor = secondaryColor;
	    slider.value = 0.5;
        slider.action = {|ez|
			var mapped = sv[1].map(ez.value);
			sourceDict[sv[0]] = mapped;
			numberBox.value = mapped;
			if(isPlaying, { sourceSynth.set(sv[0], mapped) });
		};

		randButton = Button(win, Rect(330, y + 2, 50, 30));
		randButton.font =Font(font, 12);
		randButton.states = [
			["on", textColor, secondaryColor, ],
			["off", secondaryColor, textColor ]];
		randButtons[sv[0]] = randButton;

		patternButton = Button(win, Rect(395, y + 2, 70, 30));
		patternButton.font =Font(font, 12);
		patternButton.states = [
			["include", textColor, secondaryColor, ],
			["exclude", secondaryColor, textColor ]];
		patternButtons[sv[0]] = patternButton;

		numberBox = NumberBox(win, Rect(480, y + 2, 70, 30));
		numberBox.font =Font(font, 13);
		numberBox.align = \center;
		numberBox.value =  sv[1].map(slider.value);

		sliders[sv[0]] = (slider:slider, spec:sv[1], numberBox:numberBox) ;
		y = y + 50;

        };
    };

    addControls.value(sourceVars, true);

	getSliderArgs = {|appendSymbol=false|
		var params = Array((sliders.keys.size + sourceDict.keys.size) * 2);

		sourceArgs.do {|item| params.add(item) };
		sliders.keys.do{|key|
			var item = sliders[key];
			var slider = item.slider;
			var spec = item.spec;

			if(appendSymbol, {params.add("\\" ++ key)}, {params.add(key)});
			params.add(spec.map(slider.value));
		};

		params;
	};

	// Add the buttons for playback, recording, randomization and print

	y = y + 20;
	play = Button(win.view, Rect(22, y, 105,50));
    play.states = [["Play Note", Color.black, textColor], ["Stop", Color.black, textColor]];
	play.font =Font(font, 11);
	playAction = {|val|
		var params = getSliderArgs.value();
		params.postcs;
        if (val == 1, {
			isPlaying = true;
			sourceSynth = Synth(source, params);
        },{
			isPlaying = false;
			sourceSynth.set(\gate, 0);
        });
		play.value = val;
    };
	play.action = {|button| playAction.(button.value)};

	record = Button(win.view, Rect(142, y, 100,50));
	record.font =Font(font, 11);
    record.states = [
			["Prepare Rec.", Color.red, textColor],
			["Start Rec.", Color.red, textColor],
            ["Stop Rec.", Color.red, textColor]];

    record.action = {|button|
		var str = outputDir +/+ source.replace(" ", "") ++ "_" ++ (Date.getDate).secStamp ++ ".wav";
		if(button.value == 1, { server.prepareForRecord(str)});
		if(button.value == 2, { server.record});
		if(button.value == 0, { server.stopRecording});
	};


	randomize = Button(win.view, Rect(255, y, 100,50));
    randomize.states = [["Randomize", Color.black, textColor]];
	randomize.font = Font(font, 11);
    randomize.action = {
			sliders.keys.do{|key|
				var item = sliders[key];
				var slider = item.slider;
				var numberBox = item.numberBox;
				var spec = item.spec;
				var number = 1.0.rand;
				var mappedNumber = spec.map(number);

				if(randButtons[key].value == 0, {
					slider.value = number;
					numberBox.value = mappedNumber;
					if(isPlaying, { sourceSynth.set(key, mappedNumber) })
				})
			};
	};

	print = Button(win.view, Rect(368, y, 100,50));
    print.states = [["Print", Color.black, textColor]];
	print.font =Font(font, 11);
    print.action = {|button|
		var list, params, str = "Synth(\\" ++ source;
			str = str ++ ",";
			(str + getSliderArgs.(true) ++ ")").postln;
	};

	choosePattern = PopUpMenu(win,Rect(480, y, 100, 20));
	choosePattern.font =Font(font, 9);
	choosePattern.items = patterns.collect{|pat| pat.name};

	playPattern = Button(win.view, Rect(480, y + 25, 100,25));
    playPattern.states = [["Play Pattern", Color.black, textColor], ["Stop", Color.black, textColor]];
	playPattern.font =Font(font, 10);
	playPattern.action = {|button|
		if(button.value == 0, { activePattern.stop; }, {
			var pat = Pbind(\instrument, source);
			var patArgs = patterns[choosePattern.value].args;
			var args = Array.new( (patArgs.size + sliders.keys.size) * 2 );

			args.addAll(getSliderArgs.value);
			args.add(\instrument);
			args.add(source);

			patArgs.do{|item|
				args.add(item);
			};

			args.postcs;
			pat.patternpairs = args;
			activePattern = pat.trace.play;
		});
	};

	win.view.keyDownAction = {|view, char|
		if(char.ascii == 97, { play.action.(isPlaying.not.asInt) });
		if(char.ascii == 115, { randomize.action.() });
		if(char.ascii == 100, { print.action.() });
		if(char.ascii == 102, {
			if(playPattern.value == 0, { playPattern.value = 1 }, { playPattern.value = 0});
			playPattern.action.(playPattern)
		});
	};

	win.front;

	}
}

// - use cases -
//
// x = SynthDesignerUI()
//
// x.createUI
//
// SynthDesignerUI.help
//
// ( // Simple
//
// SynthDesignerUI(
//
// 	// 1-2 Source & Source Args
// 	\cBrown, [\buf, b],
//
// 	// 3 Source Vars
// 	[
// 		[\rate, ControlSpec(0.1, 4.0, \lin, 0.01, 1)],
// 		[\pos, ControlSpec(0.0, 1.0, \lin, 0.001, 0.0)],
// 		[\atk, ControlSpec(0.001, 2.0, \lin, 0.001, 0.05)],
// 		[\amp, ControlSpec(0.0, 1.0, \lin, 0.001, 0.5)]
// 	],
// 	// 4 Patterns
// 	[
// 		(name: "First", args:[\dur, 0.5]),
// 		(name: "Third", args:[\dur, Pbrown(0.05, 0.2, 0.001)]),
// 	],
// 	// 5 server
// 	s,
// 	// 6 record path
// 	~path ++ "/output"
// )
//
// )
//
// Charvest.loadSynths(this)
//
// Synth()
//
// ( // Large
//
// SynthDesignerUI(
//
// 	// 1-2 Source & Source Args
// 	\cBrown, nil,
//
// 	// 3 Source Vars
// 	[
// 		[\rate, ControlSpec(0.1, 4.0, \lin, 0.01, 1)],
// 		[\pos, ControlSpec(0.0, 1.0, \lin, 0.001, 0.0)],
// 		[\atk, ControlSpec(0.001, 2.0, \lin, 0.001, 0.05)],
// 		[\amp, ControlSpec(0.0, 1.0, \lin, 0.001, 0.5)],
// 		[\rate, ControlSpec(0.1, 4.0, \lin, 0.01, 1)],
// 		[\pos, ControlSpec(0.0, 1.0, \lin, 0.001, 0.0)],
// 		[\atk, ControlSpec(0.001, 2.0, \lin, 0.001, 0.05)],
// 		[\amp, ControlSpec(0.0, 1.0, \lin, 0.001, 0.5)],
// 		[\rate, ControlSpec(0.1, 4.0, \lin, 0.01, 1)],
// 		[\pos, ControlSpec(0.0, 1.0, \lin, 0.001, 0.0)],
// 		[\atk, ControlSpec(0.001, 2.0, \lin, 0.001, 0.05)],
// 		[\amp, ControlSpec(0.0, 1.0, \lin, 0.001, 0.5)]
// 	],
// 	// 4 Patterns
// 	[
// 		(name: "First", args:[\dur, 0.5]),
// 		(name: "Third", args:[\dur, Pbrown(0.05, 0.2, 0.001)]),
// 	],
// 	// 5 server
// 	s,
// 	// 6 record path
// 	~path ++ "/output"
// )
//
// )