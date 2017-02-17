package fr.umlv.moduletools.main;

import java.lang.module.ModuleDescriptor;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import fr.umlv.moduletools.api.ModuleInput;
import fr.umlv.moduletools.api.ModuleOutput;
import fr.umlv.moduletools.transform.Transformer;
import fr.umlv.moduletools.transform.TransformerFactory;

class CommandLine {
  private final Supplier<ModuleDescriptor> reader;
  private final Consumer<ModuleDescriptor> writer;
  private final Transformer transformer;
  
  CommandLine(Supplier<ModuleDescriptor> reader, Consumer<ModuleDescriptor> writer, Transformer transformer) {
    this.reader = reader;
    this.writer = writer;
    this.transformer = transformer;
  }

  public Supplier<ModuleDescriptor> getReader() {
    return reader;
  }
  public Consumer<ModuleDescriptor> getWriter() {
    return writer;
  }
  public Transformer getTransformer() {
    return transformer;
  }
  
  public static CommandLine parse(String[] args) throws InvalidCommandLineException {
    Supplier<ModuleDescriptor> reader = null;
    Consumer<ModuleDescriptor> writer = null;
    Transformer transformer = Transformer.empty();
    
    for(int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.startsWith("--")) {
        Transformer t = TransformerFactory.from(arg).create(args[++i]);
        transformer = transformer.compose(t);
      } else {
        if (reader == null) {
          Optional<ModuleDescriptor> input = ModuleInput.availableInputs().read(arg);
          if (!input.isPresent()) {
            throw new InvalidCommandLineException("invalid input " + arg);
          }
          reader = input::get;
        } else {
          if (writer == null) {
            ModuleOutput output = ModuleOutput.availableOutputs();
            writer = desc -> output.write(arg, desc);
          } else {
            throw new InvalidCommandLineException("too many parameters");
          }
        }
      }
    }
    
    if (writer == null) {
      ModuleOutput output = ModuleOutput.createPrintModuleOutput(System.out);
      writer = desc -> output.write(null, desc);
    }
    if (reader == null) {
      throw new InvalidCommandLineException("no input specified");
    }
    
    return new CommandLine(reader, writer, transformer);
  }
}