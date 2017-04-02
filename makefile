SASS_SRC != find ./sass -type f -name '*.scss'

all: style.css

style.css: $(SASS_SRC)
	sassc --style compressed sass/style.scss src/main/resources/public/styles/style.css