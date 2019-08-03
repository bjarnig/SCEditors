PatternInteractionUI {

	*new {|item, server, outputDir, colorPrimary, colorSecondary|
		^super.new.display(item, server, outputDir, colorPrimary, colorSecondary)
	}

	display {|item, server, outputDir, colorPrimary, colorSecondary|

	var win, play, record, randomize, print, titleLabel, isPlaying=false;
    var sliders, randButtons, addControls, getSliderArgs, playAction;
	var pattern = item.pattern, specs=item.specs, arguments=item.arguments;
	var lowLine = 70 + ( 50 * specs.size ), y = 65;
	var textColor, knobColor, primaryColor, secondaryColor, font="Monaco";
	var currentPattern;

	// Default Colors

	textColor = Color.white;
	knobColor = Color.fromHexString("#74B3CE");
	secondaryColor = Color.fromHexString("#508991");
	primaryColor = Color.fromHexString("#172A3A");

	if(colorPrimary != nil, { primaryColor = colorPrimary});
	if(colorSecondary != nil, { secondaryColor = colorSecondary});

    // Initialize the window & dictionaries

	win = Window.new("Pattern Interaction", Rect(200, 100, 500, 160 + (50 * specs.size)));
    win.background = primaryColor;
    win.alpha = 0.95;
    win.front;

	// arguments = Dictionary();
	sliders = Dictionary();
	randButtons = Dictionary();

	titleLabel = StaticText(win, Rect(22,5,150,50));
	titleLabel.font = Font(font, 10);
	titleLabel.string = "Argument Designer";
	titleLabel.stringColor = textColor;

	win.drawFunc = {
		Pen.strokeColor = textColor;
		Pen.moveTo(20@45);
		Pen.lineTo(470@45);
		Pen.stroke;
		Pen.strokeColor = textColor;
		Pen.moveTo(20@lowLine);
		Pen.lineTo(470@lowLine);
		Pen.stroke;
	};


    // SynthDef controls & interface components

    addControls = {|isSource|

		specs.do {|spec|
		var randButton;
		var patternButton;
        var slider;
		var label;
		var numberBox;

		label = StaticText(win, Rect(35,y,100,32));
		label.font =Font(font, 14);
		label.string = spec[0];
		label.stringColor = textColor;

		slider = Slider(win, Rect(125, y, 190, 32));
		slider.background = textColor;
		slider.knobColor = secondaryColor;
	    slider.value = 0.5;
        slider.action = {|ez|
			var mapped = spec[1].map(ez.value);
			arguments[spec[0]] = mapped;
			numberBox.value = mapped;
			// if(isPlaying, { sourceSynth.set(spec[0], mapped) });
		};

		randButton = Button(win, Rect(330, y + 2, 50, 30));
		randButton.font =Font(font, 12);
		randButton.states = [
			["on", textColor, secondaryColor, ],
			["off", secondaryColor, textColor ]];
		randButtons[spec[0]] = randButton;

		// patternButton = Button(win, Rect(395, y + 2, 70, 30));
		// patternButton.font =Font(font, 12);
		// patternButton.states = [
		// 	["include", textColor, secondaryColor, ],
		// 	["exclude", secondaryColor, textColor ]];
		// patternButtons[spec[0]] = patternButton;

		numberBox = NumberBox(win, Rect(390, y + 2, 70, 30));
		numberBox.font =Font(font, 13);
		numberBox.align = \center;
		numberBox.value =  spec[1].map(slider.value);

		sliders[spec[0]] = (slider:slider, spec:spec[1], numberBox:numberBox) ;
		y = y + 50;

        };
    };

    addControls.value(true);

	/* getSliderArgs = {|appendSymbol=false|
		var params = Array((sliders.keys.size + arguments.keys.size) * 2);

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

	*/

	// Add the buttons for playback, recording, randomization and print

	y = y + 20;
	play = Button(win.view, Rect(22, y, 105,50));
    play.states = [["Play Note", Color.black, textColor], ["Stop", Color.black, textColor]];
	play.font =Font(font, 11);
	playAction = {|val|
		// var params = getSliderArgs.value();
		// params.postcs;
        if (val == 1, {
			isPlaying = true;
			currentPattern = pattern.play;
        },{
			isPlaying = false;
			currentPattern.stop;
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
		var str = outputDir +/+ "Pattern" ++ "_" ++ (Date.getDate).secStamp ++ ".wav";
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
					arguments[key] = mappedNumber;
					// if(isPlaying, { sourceSynth.set(key, mappedNumber) })
				})
			};
	};

	print = Button(win.view, Rect(368, y, 100,50));
    print.states = [["Preset", Color.black, textColor]];
	print.font =Font(font, 11);
	print.action = {|button|
		item.increasePreset();
		print.states = [["Preset: " + item.currentPreset, Color.black, textColor]];
	};

	win.view.keyDownAction = {|view, char|
		if(char.ascii == 97, { play.action.(isPlaying.not.asInt) });
		if(char.ascii == 115, { randomize.action.() });
		if(char.ascii == 100, { print.action.() });
	};

	win.front;

	}
}